package com.owl.owlyhome.radio;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.pf.ReceiveBuilder;
import akka.stream.Materializer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RadioActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    public static final String STOP = "Stop";

    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure";
    public static final String ERROR = "Error";

    private State state = State.READY;

    public final static Props props(Materializer materializer) {
        return Props.create(RadioActor.class, () -> new RadioActor(materializer));
    }

    private final Materializer materializer;

    public RadioActor(Materializer materializer) {
        this.materializer = materializer;
    }

    @Override
    public Receive createReceive() {
        return
                ReceiveBuilder
                        .create()
                        .match(
                                String.class,
                                (s) -> STOP.equals(s),
                                (s) -> stop()
                        )
                        .match(
                                Play.class,
                                (s) -> play(s)
                        )
                        .matchAny((s) -> LOG.debug("Peculiar input={}", s))
                        .build();
    }

    private Process process;
    private Thread processPrinter;

    private void play(Play req) throws ExecutionException, TimeoutException, InterruptedException {
        if (state.equals(State.READY)) {
            LOG.debug("Playing path={}", req);
            String streamUrl = getStreamUrl(req.station);

            ProcessBuilder processBuilder = new java.lang.ProcessBuilder("su", "pi", "cvlc", streamUrl);
            processBuilder.redirectErrorStream(true); // redirect error stream to output stream
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            LOG.debug("Process starting={}", processBuilder.command());

            try {
                process = processBuilder.start();
                processPrinter = new Thread() {
                    @Override
                    public void run() {
                        try {
                            InputStream is = process.getInputStream();
                            byte[] buf = new byte[1024];
                            int nr = is.read(buf);
                            while (nr != -1) {
                                System.out.write(buf, 0, nr);
                                nr = is.read(buf);
                            }
                        } catch (IOException ex) {
                            LOG.error("Error reading process", ex);
                        }
                    }

                };
                processPrinter.start();

                success(State.PLAYING);
            } catch (IOException e) {
                LOG.error("PLAY Failure", e);
                error();
            }
        } else {
            failure();
        }
    }

    private void stop() throws InterruptedException, IOException {
        if (state.equals(State.PLAYING)) {
            LOG.debug("Stopping");
            LOG.debug("Sending kill command");
            Runtime.getRuntime().exec("pkill vlc*");
            process = null;
            processPrinter.stop();
            processPrinter = null;

            success(State.READY);
        } else {
            failure();
        }
    }

    private void success(State newState) {
        state = newState;
        sender().tell(SUCCESS, self());
    }

    private void error() {
        sender().tell(ERROR, self());
    }

    private void failure() {
        sender().tell(FAILURE, self());
    }

    private String getStreamUrl(String station) throws ExecutionException, TimeoutException, InterruptedException {
        //Example url: http://yp.shoutcast.com/sbin/tunein-station.pls?id=1352469
        CompletionStage<HttpResponse> response = Http.get(context().system())
                .singleRequest(
                        HttpRequest.GET("http://yp.shoutcast.com/sbin/tunein-station.pls?id=" + station),
                        materializer
                );

        String file = response.toCompletableFuture().get(10, TimeUnit.SECONDS)
                .entity().toStrict(10, materializer)
                .toCompletableFuture().get(10, TimeUnit.SECONDS)
                .getData().decodeString(Charset.forName("UTF-8"));

        //Example file
        /*
        [playlist]
            numberofentries=1
            File1=http://104.223.59.112:80/;
            Title1=(#1 - 25589/1) All 60s All The Time: All60s, All 60s
            Length1=-1
            Version=2
         */
        String[] contents = file.split("\n");
        for (String line : contents) {
            if (line.startsWith("File1=")) {
                return line.replace("File1=", "").replace(";", "");
            }
        }

        throw new RuntimeException("Could not detect File1= in contents");
    }

    private static enum State {

        READY,
        PLAYING
    }

}
