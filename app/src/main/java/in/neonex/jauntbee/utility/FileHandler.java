package in.neonex.jauntbee.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Environment;

public class FileHandler {
    private static final long FILE_MAX_SIZE = 25 * 1024 * 1024;
    private static final String BLACKBOX_FILE = "blackbox.txt";
    private static File file;
    private static FileOutputStream outputStream;
    private static OutputStreamWriter outStreamWriter;

    private static class FileHandlerHolder {
	private static final FileHandler INSTANCE = new FileHandler();
    }

    private FileHandler() {

    }

    public static File getFilePath() {
	return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/neonex/jauntbee/blackbox/" + BLACKBOX_FILE);
    }

    public static FileHandler getInstance() {
	File root = Environment.getExternalStorageDirectory();
	File dir = new File(root.getAbsolutePath() + "/neonex/jauntbee/blackbox");
	if (!dir.exists()) {
	    dir.mkdirs();
	}
	file = new File(dir, BLACKBOX_FILE);
	return FileHandlerHolder.INSTANCE;
    }

    public static boolean isExternalStorageWritable() {
	String state = Environment.getExternalStorageState();
	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    return true;
	}
	return false;
    }

    public static boolean createNewFile(String destination) {
	boolean result = true;
	Calendar calendar = new GregorianCalendar();
	DateFormat dateFormat = DateFormat.getDateInstance();
	String header = "***JanutBee***\n\nDate: " + dateFormat.format(calendar.getTime()) + "\n" + "Destination: " + destination + "\n\n";
	try {
	    outputStream = new FileOutputStream(file);
	    outStreamWriter = new OutputStreamWriter(outputStream);
	    outputStream.write(header.getBytes());
	} catch (Exception e) {
	    result = false;
	}
	return result;
    }

    public static boolean appendFile(String text) {
	boolean result = true;
	if (file.length() < FILE_MAX_SIZE) {
	    try {
		outStreamWriter.append(text);
		outStreamWriter.flush();

	    } catch (Exception e) {
		result = false;
	    }
	} else {
	    result = false;
	}
	return result;
    }

    public static void deleteFile() {
	if (file != null && file.exists()) {
	    file.delete();
	}
	if (outStreamWriter != null && outputStream != null) {
	    try {
		outStreamWriter.close();
		outputStream.close();
	    } catch (IOException e) {

	    }

	}
    }
}
