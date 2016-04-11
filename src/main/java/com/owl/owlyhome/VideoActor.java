package com.owl.owlyhome;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import java.io.IOException;
import java.io.InputStream;

public class VideoActor extends AbstractActor {

    public final static Props props() {
        return Props.create(VideoActor.class, () -> new VideoActor());
    }

    public VideoActor() {
        receive(
                ReceiveBuilder
                .match(
                        String.class,
                        (s) -> "pause".equals(s),
                        (s) -> pause()
                )
                .match(
                        String.class,
                        (s) -> "resume".equals(s),
                        (s) -> resume()
                )
                .match(
                        String.class,
                        (s) -> "stop".equals(s),
                        (s) -> stop()
                )
                .match(
                        String.class,
                        (s) -> play(s)
                )
                .matchAny((s) -> System.out.println("Peculiar input:" + s))
                .build()
        );
    }

    private Process process;
    private Thread processPrinter;

    private void play(String s) throws IOException {
        System.out.println("Playing path:" + s);
        ProcessBuilder processBuilder = new java.lang.ProcessBuilder("omxplayer", "-o", "hdmi", s);
        processBuilder.redirectErrorStream(true); // redirect error stream to output stream
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        System.out.println("Process starting:" + processBuilder.command());

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
    }

    private void pause() throws IOException {
        System.out.println("Pausing");
        process.getOutputStream().write(" ".getBytes());
        process.getOutputStream().flush();
    }

    private void stop() throws InterruptedException, IOException {
        System.out.println("Stopping");
        System.out.println("Sending kill command");
        Runtime.getRuntime().exec("pkill omxplayer");
        process=null;
        processPrinter.stop();
        processPrinter=null;
    }

    private void resume() throws IOException {
        System.out.println("Resuming");
        process.getOutputStream().write(" ".getBytes());
        process.getOutputStream().flush();
    }

}
