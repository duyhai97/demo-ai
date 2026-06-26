package com.example.backenai.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FrameGeneratorService {

    public List<String> generate(
            String imagePath,
            String script
    ) throws Exception {

        Files.createDirectories(Paths.get("storage/frames"));

        BufferedImage product =
                ImageIO.read(new File(imagePath));

        String[] lines =
                script.split("\\.");

        List<String> frames =
                new ArrayList<>();

        int index = 1;

        for (String line : lines) {

            line = line.trim();

            if (line.isEmpty())
                continue;

            BufferedImage canvas =
                    new BufferedImage(
                            1080,
                            1920,
                            BufferedImage.TYPE_INT_RGB
                    );

            Graphics2D g =
                    canvas.createGraphics();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            //--------------------
            // Background
            //--------------------

            g.setColor(new Color(20,20,20));
            g.fillRect(0,0,1080,1920);

            //--------------------
            // Product image
            //--------------------

            Image scaled =
                    product.getScaledInstance(
                            800,
                            800,
                            Image.SCALE_SMOOTH
                    );

            Shape clip =
                    new RoundRectangle2D.Double(
                            140,
                            180,
                            800,
                            800,
                            50,
                            50
                    );

            g.setClip(clip);

            g.drawImage(
                    scaled,
                    140,
                    180,
                    null
            );

            g.setClip(null);

            //--------------------
            // Caption
            //--------------------

            g.setColor(Color.WHITE);

            g.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            64
                    )
            );

            drawCenter(
                    g,
                    line,
                    1080,
                    1150
            );

            //--------------------
            // Button
            //--------------------

            g.setColor(
                    new Color(255,40,90)
            );

            g.fillRoundRect(
                    280,
                    1550,
                    520,
                    120,
                    60,
                    60
            );

            g.setColor(Color.WHITE);

            g.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            52
                    )
            );

            drawCenter(
                    g,
                    "MUA NGAY",
                    1080,
                    1630
            );

            g.dispose();

            String file =
                    "storage/frames/frame"
                            + String.format("%03d", index++)
                            + "_"
                            + UUID.randomUUID()
                            + ".png";

            ImageIO.write(
                    canvas,
                    "png",
                    new File(file)
            );

            frames.add(file);
        }

        return frames;
    }

    private void drawCenter(
            Graphics2D g,
            String text,
            int width,
            int y
    ){

        FontMetrics fm =
                g.getFontMetrics();

        int x =
                (width - fm.stringWidth(text))/2;

        g.drawString(
                text,
                x,
                y
        );
    }

}