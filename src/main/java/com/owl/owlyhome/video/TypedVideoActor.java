package com.owl.owlyhome.video;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import com.owl.owlyhome.AudioOption;

import java.io.IOException;
import java.io.InputStream;

public class TypedVideoActor {

    private static void failure(ActorRef<GenericResponse> sender) {
        sender.tell(FAILURE);
    }

    private static void error(ActorRef<GenericResponse> sender) {
        sender.tell(ERROR);
    }

    private static void success(ActorRef<GenericResponse> sender) {
        sender.tell(SUCCESS);
    }

    private static Behavior<VideoCommand> playVideo(State state, ActorContext<VideoCommand> ctx, PlayVideo req) {
        if (!State.READY.equals(state)) {
            ctx.getLog().error("Attempt to play when not ready");
            failure(req.sender);
            return Behaviors.same();
        }

        ctx.getLog().debug("Playing path={}", req);
        ProcessBuilder processBuilder = new ProcessBuilder("omxplayer", "-o", req.audio.commandLine, req.filename);
        processBuilder.redirectErrorStream(true); // redirect error stream to output stream
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        ctx.getLog().debug("Process starting={}", processBuilder.command());

        Process newProcess;
        Thread newProcessPrinter;
        try {
            newProcess = processBuilder.start();
            newProcessPrinter = new Thread(() -> {
                try {
                    InputStream is = newProcess.getInputStream();
                    byte[] buf = new byte[1024];
                    int nr = is.read(buf);
                    while (nr != -1) {
                        System.out.write(buf, 0, nr);
                        nr = is.read(buf);
                    }
                } catch (IOException ex) {
                    ctx.getLog().error("Error reading process", ex);
                }
            });
            newProcessPrinter.start();

            success(req.sender);
            return behavior(State.PLAYING, newProcess, newProcessPrinter);
        } catch (IOException e) {
            ctx.getLog().error("PLAY Failure", e);

            error(req.sender);
            return Behaviors.same();
        }
    }

    private enum State {

        READY,
        PLAYING,
        PAUSED
    }

    public interface GenericResponse {

    }

    public interface VideoCommand {
    }

    public static final class PlayVideo implements VideoCommand {
        public final ActorRef<GenericResponse> sender;
        public final String filename;
        public final AudioOption audio;

        public PlayVideo(ActorRef<GenericResponse> sender, String filename, AudioOption audio) {
            this.sender = sender;
            this.filename = filename;
            this.audio = audio;
        }

        @Override
        public String toString() {
            return "Play{" + "filename=" + filename + ", audio=" + audio + '}';
        }
    }

    public static final class StopVideo implements VideoCommand {
        public final ActorRef<GenericResponse> sender;

        public StopVideo(ActorRef<GenericResponse> sender) {
            this.sender = sender;
        }
    }

    public static final class PauseVideo implements VideoCommand {
        public final ActorRef<GenericResponse> sender;

        public PauseVideo(ActorRef<GenericResponse> sender) {
            this.sender = sender;
        }
    }

    public static final class ResumeVideo implements VideoCommand {
        public final ActorRef<GenericResponse> sender;

        public ResumeVideo(ActorRef<GenericResponse> sender) {
            this.sender = sender;
        }
    }

    public static final Behavior<VideoCommand> BEHAVIOR = behavior(State.READY, null, null);

    private static final Behavior<VideoCommand> behavior(State state, Process process, Thread processPrinter) {
        return Behaviors
                .receive(VideoCommand.class)
                .onMessage(PlayVideo.class, (ctx, msg) -> playVideo(state, ctx, msg))
                .onMessage(PauseVideo.class, (ctx, msg) -> pauseVideo(state, ctx, msg, process, processPrinter))
                .onMessage(ResumeVideo.class, (ctx, msg) -> resumeVideo(state, ctx, msg, process, processPrinter))
                .onMessage(StopVideo.class, (ctx, msg) -> stopVideo(state, ctx, msg, process, processPrinter))
                .build();
    }

    private static Behavior<VideoCommand> stopVideo(State state, ActorContext<VideoCommand> ctx, StopVideo req, Process process, Thread processPrinter) throws IOException {
        if (!State.PLAYING.equals(state) && !State.PAUSED.equals(state)) {
            ctx.getLog().error("Attempt to play when not ready");
            failure(req.sender);
            return Behaviors.same();
        }

        ctx.getLog().debug("Stopping");
        Runtime.getRuntime().exec("pkill omxplayer");
        process = null;
        processPrinter.stop();
        processPrinter = null;

        success(req.sender);
        return behavior(State.READY, process, processPrinter);
    }


    private static Behavior<VideoCommand> resumeVideo(State state, ActorContext<VideoCommand> ctx, ResumeVideo req, Process process, Thread processPrinter) throws IOException {
        if (!State.PAUSED.equals(state)) {
            ctx.getLog().error("Attempt to play when not ready");
            failure(req.sender);
            return Behaviors.same();
        }

        ctx.getLog().debug("Resuming");
        process.getOutputStream().write(" ".getBytes());
        process.getOutputStream().flush();

        success(req.sender);
        return behavior(State.PLAYING, process, processPrinter);
    }

    private static Behavior<VideoCommand> pauseVideo(State state, ActorContext<VideoCommand> ctx, PauseVideo req, Process process, Thread processPrinter) throws IOException {
        if (!State.PLAYING.equals(state)) {
            ctx.getLog().error("Attempt to play when not ready");
            failure(req.sender);
            return Behaviors.same();
        }

        ctx.getLog().debug("Pausing");
        process.getOutputStream().write(" ".getBytes());
        process.getOutputStream().flush();

        success(req.sender);
        return behavior(State.PAUSED, process, processPrinter);
    }
}
