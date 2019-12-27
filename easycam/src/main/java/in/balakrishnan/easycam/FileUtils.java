package in.balakrishnan.easycam;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import in.balakrishnan.easycam.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by BalaKrishnan on 2019-07-15.
 */
public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * This function delete all the files in local storage, that are created to while using this module
     *
     * @param context    Context to created file
     * @param bucketName Bucket name where file is located
     */
    public static void clearAllFiles(Context context, String bucketName) {
        String parentFolder = (String) context.getText(R.string.app_name);

        File externalFilesDir;
        if (TextUtils.isEmpty(bucketName)) {
            externalFilesDir = context.getExternalFilesDir(parentFolder);
        } else {
            externalFilesDir = context.getExternalFilesDir(parentFolder + File.separator + bucketName);
        }

        deleteRecursive(externalFilesDir);
    }

    /**
     * This function used to delete directory or file recursively
     *
     * @param fileOrDirectory Directory / File
     */
    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * This function will delete the given file in given bucket
     *
     * @param context    Context to created file
     * @param bucketName Bucket name where file is located
     * @param fileName   Name of the file to be deleted
     */
    public static void clearFile(Context context, String bucketName, String fileName) {
        try {
            String parentFolder = (String) context.getText(R.string.app_name);
            File externalFilesDir;
            if (TextUtils.isEmpty(bucketName)) {
                externalFilesDir = context.getExternalFilesDir(parentFolder);
            } else {
                externalFilesDir = context.getExternalFilesDir(parentFolder + File.separator + bucketName);
            }
            File file = new File(externalFilesDir, fileName);
            if (file.exists()) {
                file.delete();
            } else {
                Log.e(TAG, "clearFile: File not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearFile(Context context, String bucketName, String prefix, String fileName) {
        Log.d(TAG, "clearFile: name" + fileName);
        clearFile(context, bucketName, prefix + fileName);
    }


    /**
     * This function will return a file with given input
     *
     * @param context    Context to created file
     * @param bucketName Bucket name where file is created
     * @param fileName   Name of the file to be created
     * @return File
     */
    public static File getFile(Context context, String bucketName, String fileName) {
        return getFile(context, bucketName, fileName, true);
    }


    public static File getFile(Context context, String bucketName, String fileName, boolean isCaptured) {
        String parentFolder = (String) context.getText(R.string.app_name);
        File externalFilesDir;
        if (TextUtils.isEmpty(bucketName)) {
            externalFilesDir = context.getExternalFilesDir(parentFolder);
        } else {
            externalFilesDir = context.getExternalFilesDir(parentFolder + File.separator + bucketName);
        }
        Log.d(TAG, String.format("getFile: original name %s isCaptured %b", fileName, isCaptured));
        if (!fileName.startsWith("Captured_") && isCaptured)
            fileName = "Captured_" + fileName;
        else if (!fileName.startsWith("Added_"))
            fileName = "Added_" + fileName;
        Log.d(TAG, String.format("getFile: created file name %s ", fileName));

        return new File(externalFilesDir, fileName);
    }


    /**
     * This function will return all the files in the bucket
     *
     * @param context    context is used to access the external storage
     * @param bucketName name of the bucket to get files
     * @return File array
     */
    public static File[] getAllFiles(Context context, String bucketName) {
        String parentFolder = (String) context.getText(R.string.app_name);
        File directory;
        if (TextUtils.isEmpty(bucketName)) {
            directory = context.getExternalFilesDir(parentFolder);
        } else {
            directory = context.getExternalFilesDir(parentFolder + File.separator + bucketName);
        }
        return directory.listFiles();
    }

    /**
     * This function will return the path of the bucket
     *
     * @param context    context is used to access the external storage
     * @param bucketName bucket to be get the path
     * @return path of the bucket
     */
    public static String getFilePath(Context context, String bucketName) {
        String parentFolder = (String) context.getText(R.string.app_name);
        String externalFilesDir;
        if (TextUtils.isEmpty(bucketName)) {
            externalFilesDir = context.getExternalFilesDir(parentFolder).getPath();
        } else {
            externalFilesDir = context.getExternalFilesDir(parentFolder + File.separator + bucketName).getPath();
        }
        return externalFilesDir;
    }

    /**
     * SAves bitmap into File location
     *
     * @param context    Context to access external storage
     * @param bitmap     Bitmap to be stored
     * @param bucketName bucket to be stored
     * @param fileName   name of the file to be created and saved
     */
    public static void saveBitmapForEditPost(Context context, Bitmap bitmap, String bucketName, String fileName, boolean isCaptured) {
        try {
            File file = FileUtils.getFile(context, bucketName, fileName, isCaptured);
            FileOutputStream originalStream = null;
            originalStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, originalStream);
            //   bitmap.recycle();
            originalStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

    public static void renameFile(@NonNull Context context, String from, String to) {
        File oldFolder = new File(FileUtils.getFilePath(context, from));
        File newFolder = new File(FileUtils.getFilePath(context, to));
        oldFolder.renameTo(newFolder);
    }

}
