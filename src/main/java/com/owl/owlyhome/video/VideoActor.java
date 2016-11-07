package com.owl.owlyhome.video;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import java.io.IOException;
import java.io.InputStream;

public class VideoActor extends AbstractActor {

    public static final String PAUSE = "Pause";
    public static final String RESUME = "Resume";
    public static final String STOP = "Stop";

    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure";
    public static final String ERROR = "Error";

    private State state = State.READY;

    public final static Props props() {
        return Props.create(VideoActor.class, () -> new VideoActor());
    }

    public VideoActor() {
        receive(
                ReceiveBuilder
                .match(
                        String.class,
                        (s) -> PAUSE.equals(s),
                        (s) -> pause()
                )
                .match(
                        String.class,
                        (s) -> RESUME.equals(s),
                        (s) -> resume()
                )
                .match(
                        String.class,
                        (s) -> STOP.equals(s),
                        (s) -> stop()
                )
                .match(
                        Play.class,
                        (s) -> play(s)
                )
                .matchAny((s) -> System.out.println("Peculiar input:" + s))
                .build()
        );
    }

    private Process process;
    private Thread processPrinter;

    private void play(Play req) {
        if (state.equals(State.READY)) {
            System.out.println("Playing path:" + req);
            ProcessBuilder processBuilder = new java.lang.ProcessBuilder("omxplayer", "-o", req.audio.commandLine, req.filename);
            processBuilder.redirectErrorStream(true); // redirect error stream to output stream
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            System.out.println("Process starting:" + processBuilder.command());

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
                            ex.printStackTrace();
                        }
                    }

                };
                processPrinter.start();

                success(State.PLAYING);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        } else {
            failure();
        }
    }

    private void pause() throws IOException {
        if (state.equals(State.PLAYING)) {
            System.out.println("Pausing");
            process.getOutputStream().write(" ".getBytes());
            process.getOutputStream().flush();

            success(State.PAUSED);
        } else {
            failure();
        }
    }

    private void stop() throws InterruptedException, IOException {
        if (state.equals(State.PLAYING) || state.equals(State.PAUSED)) {
            System.out.println("Stopping");
            System.out.println("Sending kill command");
            Runtime.getRuntime().exec("pkill omxplayer");
            process = null;
            processPrinter.stop();
            processPrinter = null;

            success(State.READY);
        } else {
            failure();
        }
    }

    private void resume() throws IOException {
        if (state.equals(State.PAUSED)) {
            System.out.println("Resuming");
            process.getOutputStream().write(" ".getBytes());
            process.getOutputStream().flush();

            success(State.PLAYING);
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

    private static enum State {

        READY,
        PLAYING,
        PAUSED
    }

}
