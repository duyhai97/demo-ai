package com.example.backenai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoRenderService {

    public String render(
            List<String> frames,
            String voicePath
    ) throws Exception {

        Files.createDirectories(
                Path.of("storage/videos")
        );

        Files.createDirectories(
                Path.of("storage/tmp")
        );

        String listFile =
                "storage/tmp/frame_"
                        + UUID.randomUUID()
                        + ".txt";

        String output =
                "storage/videos/video_"
                        + UUID.randomUUID()
                        + ".mp4";

        StringBuilder sb =
                new StringBuilder();

        for (String frame : frames) {

            sb.append("file '")
                    .append(
                            Path.of(frame)
                                    .toAbsolutePath()
                                    .toString()
                                    .replace("\\","/")
                    )
                    .append("'\n");

            sb.append("duration 2\n");

        }

        sb.append("file '")
                .append(
                        Path.of(
                                        frames.get(frames.size()-1)
                                ).toAbsolutePath()
                                .toString()
                                .replace("\\","/")
                )
                .append("'");

        Files.writeString(
                Path.of(listFile),
                sb.toString()
        );

        ProcessBuilder pb =
                new ProcessBuilder(

                        "ffmpeg",

                        "-y",

                        "-f",
                        "concat",

                        "-safe",
                        "0",

                        "-i",
                        listFile,

                        "-i",
                        voicePath,

                        "-vsync",
                        "vfr",

                        "-pix_fmt",
                        "yuv420p",

                        "-c:v",
                        "libx264",

                        "-c:a",
                        "aac",

                        "-shortest",

                        output
                );

        pb.redirectErrorStream(true);

        System.out.println("==================");
        System.out.println("FFMPEG");
        pb.command().forEach(System.out::println);
        System.out.println("==================");

        Process process =
                pb.start();

        try(BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream()
                            )
                    )){

            String line;

            while((line=br.readLine())!=null){

                System.out.println(
                        "[FFMPEG] " + line
                );

            }

        }

        int exit =
                process.waitFor();

        if(exit!=0){

            throw new RuntimeException(
                    "FFmpeg render failed"
            );

        }

        return output;

    }

}