package usblock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ReadOnly {

	public static BufferedReader inp;
	public static BufferedWriter out;

	public static void main(String[] args) throws IOException {
		checkArg(args);
		
		Boolean mode = args[0].equals("on") ? true : false;
		String drive = args[1];

		String readonly = "select volume %\natt vol set readonly";
		String removeReadonly = "select volume %\natt vol clear readonly";
		readonly = readonly.replace("%", drive);
		removeReadonly = removeReadonly.replace("%", drive);

		String currentDir = new java.io.File(".").getCanonicalPath();
		writeScriptFile(currentDir + "\\readonly", readonly);
		writeScriptFile(currentDir + "\\removeReadonly", removeReadonly);

		String cmd = "DISKPART /s " + (mode ? currentDir + "\\readonly" : currentDir + "\\removeReadonly");

		try {
			System.out.println("Starting Diskpart");
			Process p = Runtime.getRuntime().exec(cmd);
			System.out.println("Setting read only = " + mode + " for drive " + drive);
			inp = new BufferedReader(new InputStreamReader(p.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			String outs;
			String louts = null;
			while ((outs = inp.readLine()) != null) {
				louts = outs;
				if (outs.equals("The operation is not supported on removable media.")) {
					break;
				}
			}
			inp.close();
			out.close();
			interpretLoutsAndExit(louts);
		} catch (IOException err) {
			System.err.println("This tool needs admin privileges.");
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	private static void interpretLoutsAndExit(String louts) {
		if (louts.equals("Volume attributes set successfully.")) {
			System.out.println("Read only set successfully");
			System.exit(0);
		} else if (louts.equals("Volume attributes cleared successfully.")) {
			System.out.println("Read only removed successfully");
			System.exit(0);
		} else {
			if (louts.equals("There is no volume selected.")) {
				System.err.println("Error, drive not found. Are you sure that this drive letter exists?");
			}else{
				System.err.println(louts);
			}
			System.exit(-1);
		}
	}

	private static void checkArg(String[] args) {
		if (args.length != 2 || (!args[0].equals("on") && !args[0].equals("off"))
				|| !args[1].toLowerCase().matches("[a-z]")) {
			System.err.println("Usage: usblock {on/off} {drive letter}");
			System.exit(-1);
		}
	}

	public static void writeScriptFile(String path, String content) {
		try {
			PrintWriter writer = new PrintWriter(path, "UTF-8");
			writer.print(content);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
