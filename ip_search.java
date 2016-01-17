package Search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ip_search extends Thread {

	private String ans1 = "";
	private String ans2 = "";
	private String ans3 = "";
	private int ans4;
	private int n = -1;
	private int p;
	private int nold;
	private int timeout;
	private ArrayList<String> nameList;
	private ArrayList<String> ipList;

	@SuppressWarnings("resource")
	public ip_search() {

		while (!ans1.equals("n") && !ans1.equals("y")) {
			Scanner desc1 = new Scanner(System.in);
			System.out.println("Do you want the ip addresses? y/n");
			ans1 = desc1.next();

		}

		while (!ans2.equals("n") && !ans2.equals("y")) {
			Scanner desc2 = new Scanner(System.in);
			System.out.println("Do you want a counter? y/n");
			ans2 = desc2.next();
		}

		while (!ans3.equals("n") && !ans3.equals("y")) {
			Scanner desc3 = new Scanner(System.in);
			System.out
					.println("Do you want notifications when someone connects/disconnects? y/n");
			ans3 = desc3.next();
		}

		boolean corr = false;

		while (!corr) {

			try {

				Scanner desc4 = new Scanner(System.in);
				System.out.println("In what intervals? 0-inf");
				ans4 = desc4.nextInt();
				corr = true;

			} catch (InputMismatchException e) {

				System.out.println("Input integer");
				
			}
		}
		corr = false;
		while (!corr) {

			try {

				Scanner desc5 = new Scanner(System.in);
				System.out.println("What timeout? 0-inf");
				timeout = desc5.nextInt();
				corr = true;

			} catch (InputMismatchException e) {

				System.out.println("Input integer");
			}

		}
	}

	public void run() {
		
		p = 4;

		if (ans4 > 0) {

			while (true) {

				print();

				try {
					Thread.sleep(ans4);
				} catch (InterruptedException e) {

					System.out.println("Unable to sleep");

					e.printStackTrace();
				}
			}

		} else {

			print();

		}
	}

	private void print() {

		search();

		System.out.println();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(Calendar.getInstance().getTime());
		System.out.println(timeStamp);
		System.out.println();

		if (ans1.equals("y")) {

			System.out.println(ipList);
			System.out.println(nameList);

		}

		if (ans2.equals("y")) {

			nold = n;
			n = ipList.size();
			System.out.println("Number of connected devices is: " + n);
		}

		if (ans3.equals("y") && ans2.equals("y")) {

			if (n - nold > 0 && nold >= 0) {

				System.out.println("Nbr of new connections is: " + (n - nold));
			}
			if (n - nold < 0 && nold >= 0) {

				System.out.println("Nbr of new disconnections is: "
						+ (n - nold));
			}

		} else if (ans3.equals("y") && !ans2.equals("y")) {

			nold = n;
			n = ipList.size();

			if (n - nold > 0 && nold >= 0) {

				System.out.println("Nbr of new connections is: " + (n - nold));
			}
			if (n - nold < 0 && nold >= 0) {

				System.out.println("Nbr of new disconnections is: "
						+ (n - nold));
			}
		}
	}

	public void search() {

		ipList = new ArrayList<String>();
		nameList = new ArrayList<String>();

		String ipAddress = "192.168.1.";

		for (int i = 2; i < 255; i++) {

			InetAddress inet;

			try {

				inet = InetAddress.getByName(ipAddress + i);

				if (inet.isReachable(timeout)) {

					ipList.add(ipAddress + i);

					//Might need to change to "arp -a" to get the right respons
					String cmd = "arp " + ipAddress + i;
					String s = null;

					try {

						Process p = Runtime.getRuntime().exec(cmd);

						BufferedReader stdInput = new BufferedReader(
								new InputStreamReader(p.getInputStream()));

						while ((s = stdInput.readLine()) != null) {
							String[] split = s.split(" ");

							if (split[3].contains(":")) {
								nameList.add(lookup(split[3]));
							} else {
								nameList.add("?");
							}
						}

					} catch (IOException e) {
						System.out.println("arp error");
						System.exit(0);
					}

				}

			} catch (UnknownHostException e) {

				System.out.println("Unknown host error");

			} catch (IOException e) {

				System.out.println("Network error");
				try {
					Thread.sleep((int) Math.pow(10, p));
					p++;
					//Exponential back off
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				i--;
			}

		}

	}

	private String lookup(String look) {

		String dir = System.getProperty("user.dir");

		try (BufferedReader br = new BufferedReader(
				new FileReader(dir + "/MAC"))) {

			String line = null;

			while ((line = br.readLine()) != null) {

				try {

					String[] lineSplit = line.split(" ");

					if (lineSplit[0].equals(look)) {

						return lineSplit[1];

					}

				} catch (ArrayIndexOutOfBoundsException e) {

					return "?";

				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("No MAC file");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "?";
	}

	public static void main(String[] args) throws java.net.ConnectException {

		ip_search as = new ip_search();
		as.start();

	}

}
