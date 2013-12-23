package overbot;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import shared.Wiki;
import shared.WikiPage;

public class OverBot {

	public static void main(String[] args) {

		System.out.println("v13.12.23");

		String[] expectedArgs = { "username", "continueKey" };
		String[] expectedArgsDescription = {
				"username is your username on the wiki.",
				"continueKey is the file where to continue from "
						+ "(equals the name of the last edited file or \"\")." };
		if (args.length != expectedArgs.length) {
			System.out.print("Usage: java -jar filename.jar");
			for (String i : expectedArgs)
				System.out.print(" [" + i + "]");
			System.out.println("");
			for (String i : expectedArgsDescription)
				System.out.println("Where " + i);
			System.exit(-1);
		}
		Wiki commons = new Wiki("commons.wikimedia.org");
		try {
			System.out.println("Please type in the password for " + args[0]
					+ ".");
			commons.login(args[0], System.console().readPassword());
			// Minimum time between edits in ms
			commons.setThrottle(5 * 1000);
			// Pause bot if lag is greater than ... in s
			commons.setMaxLag(3);
			commons.setMarkMinor(true);
			commons.setMarkBot(false);
			cleanup(commons, args[1]);
		} catch (LoginException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Do cleanup for all items in a given category for a given wiki
	 * 
	 * @param wiki
	 *            Target wiki
	 * @param continueKey
	 *            The key to start with (equals the last edited file)
	 * @throws IOException
	 * @throws LoginException
	 */
	private static void cleanup(Wiki wiki, String continueKey)
			throws IOException, LoginException {

		Object[] nextBatchObjects;
		String[] nextBatch;
		continueKey = continueKey.replace(' ', '_');
		long total = 0;
		long startTime = System.currentTimeMillis();

		while (true) {
			nextBatchObjects = wiki.listAllFiles(continueKey, 15);
			nextBatch = (String[]) nextBatchObjects[1];
			continueKey = (String) nextBatchObjects[0];
			total += nextBatch.length;

			for (String i : nextBatch) {
				if ((wiki.getPageInfo(i).get("protection")).toString()
						.contains("edit=sysop"))
					continue;
				WikiPage target = new WikiPage(wiki, i);
				target.cleanupOvercat(1, true);
				target.writeText();
			}
			{
				long runtime = (System.currentTimeMillis() - startTime) / 1000;
				long days = runtime / (60 * 60 * 24);
				long hours = (runtime % (60 * 60 * 24)) / (60 * 60);
				long minutes = (runtime % (60 * 60)) / 60;
				long seconds = runtime % 60;
				float avg = ((float) runtime / (float) total);
				System.out.println("\nStatus:\n" + total + " files crawled in "
						+ days + " days " + hours + " hours " + minutes
						+ " minutes " + seconds + " seconds. Thus it took "
						+ (total == 0 ? "Inf" : avg) + " seconds per file.\n");
			}
			if (continueKey.length() == 0)
				break; // No next batch available
			System.out
					.println("Requesting next batch of files to work with. (Continue from "
							+ continueKey + ")\n");
		}
		System.out.println("All batches done. Exiting.");
	}
}
