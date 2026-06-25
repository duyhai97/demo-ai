package com.example.backenai.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.awt.List.*;

@Service
public class VideoRenderService {

    public String render(
            String imagePath,
            String voicePath,
            String subtitlePath
    ) throws Exception {
        System.out.println("IMAGE = " + imagePath);
        System.out.println("VOICE = " + voicePath);
        System.out.println("SUB = " + subtitlePath);

        System.out.println(
                "VOICE EXISTS = " +
                        Files.exists(Paths.get(voicePath))
        );
        Files.createDirectories(
                Paths.get(
                        "storage/videos"
                )
        );

        String output =
                "storage/videos/video_"
                        + UUID.randomUUID()
                        + ".mp4";

        List cmd = List.of(
                        "ffmpeg",
                        "-y",

                        "-loop","1",
                        "-i",imagePath,

                        "-i",voicePath,

                        "-vf",
                        "scale=1080:1920," +
                                "zoompan=z='min(zoom+0.0015,1.2)':d=125," +
                                "subtitles=" +
                                subtitlePath.replace("\\","/"),

                        "-shortest",

                        "-c:v","libx264",

                        output
                );

        ProcessBuilder pb =
                new ProcessBuilder(cmd);

        pb.redirectErrorStream(true);

        Process p = pb.start();

        try (
                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(
                                        p.getInputStream()
                                )
                        )
        ) {

            String line;

            while ((line = br.readLine()) != null) {

                System.out.println(
                        "[FFMPEG] " + line
                );
            }
        }

        int code = p.waitFor();

        if (code != 0) {

            throw new RuntimeException(
                    "FFmpeg failed"
            );
        }

        return output;
    }

    private void createFolders() {

        new File("storage").mkdirs();
        new File("storage/videos").mkdirs();
        new File("storage/images").mkdirs();
    }

    private File createCaptionImage(
            String productName
    ) throws Exception {

        int width = 1080;
        int height = 1920;

        BufferedImage image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D g =
                image.createGraphics();

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // background
        g.setColor(Color.BLACK);
        g.fillRect(
                0,
                0,
                width,
                height
        );

        // title
        g.setColor(Color.WHITE);

        g.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        72
                )
        );

        drawCenteredText(
                g,
                productName,
                width,
                700
        );

        g.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        42
                )
        );

        drawCenteredText(
                g,
                "San pham dang hot tren TikTok",
                width,
                850
        );

        drawCenteredText(
                g,
                "Nhan vao gio hang de mua",
                width,
                950
        );

        g.dispose();

        File imageFile =
                new File(
                        "storage/images/caption_"
                                + UUID.randomUUID()
                                + ".png"
                );

        ImageIO.write(
                image,
                "png",
                imageFile
        );

        return imageFile;
    }

    private void drawCenteredText(
            Graphics2D g,
            String text,
            int width,
            int y
    ) {

        FontMetrics fm =
                g.getFontMetrics();

        int x =
                (width - fm.stringWidth(text))
                        / 2;

        g.drawString(
                text,
                x,
                y
        );
    }
}