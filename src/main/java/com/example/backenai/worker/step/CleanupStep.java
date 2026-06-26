package com.example.backenai.worker.step;

import com.example.backenai.model.VideoJob;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class CleanupStep implements VideoStep {

    @Override
    public void execute(VideoJob job) {

        deleteFiles(job.getFramePaths());

        deleteFile(job.getVoicePath());

        deleteFolderChildren("storage/tmp");

        System.out.println("CLEANUP DONE");
    }

    private void deleteFiles(Iterable<String> paths) {
        if (paths == null) {
            return;
        }

        for (String path : paths) {
            deleteFile(path);
        }
    }

    private void deleteFile(String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        try {
            File file = new File(path);

            if (file.exists() && file.isFile()) {
                boolean deleted = file.delete();
                System.out.println("DELETE FILE " + path + " = " + deleted);
            }
        } catch (Exception e) {
            System.out.println("DELETE FILE FAILED = " + path);
            e.printStackTrace();
        }
    }

    private void deleteFolderChildren(String folderPath) {
        try {
            File folder = new File(folderPath);

            if (!folder.exists() || !folder.isDirectory()) {
                return;
            }

            File[] files = folder.listFiles();

            if (files == null) {
                return;
            }

            for (File file : files) {
                if (file.isFile()) {
                    boolean deleted = file.delete();
                    System.out.println("DELETE TMP " + file.getAbsolutePath() + " = " + deleted);
                }
            }
        } catch (Exception e) {
            System.out.println("DELETE TMP FAILED");
            e.printStackTrace();
        }
    }
}