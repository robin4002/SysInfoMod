package fr.mcnanotech.sysinfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "sysinfo", name = "System Information", version = "1.0.4")
public class SysInfo
{
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		ArrayList<String> info = new ArrayList<String>();

		info.add("--- System.getProperty ---");
		info.add("Java vendor : " + System.getProperty("java.vendor"));
		info.add("Java version : " + System.getProperty("java.version"));
		info.add("OS arch : " + System.getProperty("os.arch"));
		info.add("OS name : " + System.getProperty("os.name"));
		info.add("OS version : " + System.getProperty("os.version"));

		info.add("--- Runtime information ---");
		info.add("Number of core : " + Runtime.getRuntime().availableProcessors());
		info.add("Free memory (mo): " + Runtime.getRuntime().freeMemory() / (1024 * 1024));
		info.add("Maximum memory (mo): " + (Runtime.getRuntime().maxMemory() == Long.MAX_VALUE ? "no limit" : Runtime.getRuntime().maxMemory() / (1024 * 1024)));
		info.add("Total memory (mo): " + Runtime.getRuntime().totalMemory() / (1024 * 1024));

		info.add("--- JVM arg ---");
		info.add(getJVMFlag());

		info.add("--- Disk information ---");
		File[] roots = File.listRoots();
		for(File root : roots)
		{
			info.add("File system root: " + root.getAbsolutePath());
			info.add("Total space (mo): " + root.getTotalSpace() / (1024 * 1024));
			info.add("Free space (mo): " + root.getFreeSpace() / (1024 * 1024));
			info.add("Usable space (mo): " + root.getUsableSpace() / (1024 * 1024));
		}

		info.add("--- System information ---");
		if(getOSType() == EnumOS.LINUX)
		{
			executeCommand(info, "uname -a");
			executeCommand(info, "ps aux");
			executeCommand(info, "free -m");
			executeCommand(info, "cat /proc/cpuinfo");
		}
		else if(getOSType() == EnumOS.WINDOWS)
		{
			executeCommand(info, "SYSTEMINFO");
			executeCommand(info, "tasklist.exe /fo csv /nh");
		}
		info.add("--- SysInfo finish ---");

		File sysInfoDir = new File(".", "SysInfo");
		sysInfoDir.mkdirs();
		Date date = new Date();
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String fileName = shortDateFormat.format(date).replace('/', '.').replace(' ', '-').replace(':', '.') + ".txt";
		File target = new File(sysInfoDir, fileName);
		try
		{
			target.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(target));
			for(String str : info)
			{
				bw.write(str);
				bw.newLine();
			}
			bw.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		info.clear();
	}

	private ArrayList<String> executeCommand(ArrayList<String> list, String command)
	{
		try
		{
			String line;
			Process p = Runtime.getRuntime().exec(command);
			list.add("-- Running command : " + command + " --");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while((line = input.readLine()) != null)
			{
				list.add(line);
			}
			input.close();
			list.add("-- End of the command --");
		}
		catch(Exception err)
		{
			System.out.println(err.getMessage());
		}
		return list;
	}

	private String getJVMFlag()
	{
		RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
		List list = runtimemxbean.getInputArguments();
		int i = 0;
		StringBuilder stringbuilder = new StringBuilder();
		Iterator iterator = list.iterator();

		while(iterator.hasNext())
		{
			String s = (String)iterator.next();

			if(s.startsWith("-X"))
			{
				if(i++ > 0)
				{
					stringbuilder.append(" ");
				}

				stringbuilder.append(s);
			}
		}

		return String.format("%d total; %s", new Object[] {Integer.valueOf(i), stringbuilder.toString()});
	}

	private EnumOS getOSType()
	{
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.MACOS : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
	}

	private enum EnumOS
	{
		LINUX, SOLARIS, WINDOWS, MACOS, UNKNOWN;
	}
}