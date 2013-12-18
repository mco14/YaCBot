package overbot;

import java.io.IOException;
import javax.security.auth.login.LoginException;

import shared.Wiki;
import shared.WikiPage;

public class OverBot {

	public static void main(String[] args) {

		System.out.println("v13.12.15");

		String[] expectedArgs = { "username", "category" };
		String[] expectedArgsDescription = {
				"username is your username on the wiki.",
				"category is the category which itmes (files or subcategories) should be cleaned up" };

		if (args.length != expectedArgs.length) {
			System.out.print("Usage: java -jar filename.jar");
			for (int i = 0; i < expectedArgs.length; i++) {
				System.out.print(" [" + expectedArgs[i] + "]");
			}
			System.out.println("");
			for (int i = 0; i < expectedArgsDescription.length; i++) {
				System.out.println("Where " + expectedArgsDescription[i]);
			}
			System.exit(-1);
		}
		Wiki commons = new Wiki("commons.wikimedia.org");
		try {
			System.out.println("Please type in the password for " + args[0]
					+ ".");
			commons.login(args[0], System.console().readPassword());
			// Minimum time between edits in ms
			commons.setThrottle(0 * 1000);
			// Pause bot if lag is greater than ... in s
			commons.setMaxLag(2);
			cleanup(commons, args[1], args[0]);
		} catch (LoginException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Do cleanup for all items in a given category for a given wiki
	 * 
	 * @param wiki
	 *            Target wiki
	 * @param category
	 *            The category to be cleaned up
	 * @throws IOException
	 * @throws LoginException
	 */
	private static void cleanup(Wiki wiki, String category, String user)
			throws IOException, LoginException {

		String[] categoryMembers = wiki.getCategoryMembers(category);

		for (int i = 0; i < categoryMembers.length; i++) {
			if (wiki.getLastEditor(categoryMembers[i]).equals(user))
				continue;
			if (categoryMembers[i].startsWith("Category:")) {
				// may cause infinite loop -> ignore for now
				System.out.println("Found a subcategory " + categoryMembers[i]
						+ " which is ignored.");
				// first cleanup categoryMembers[i];
				// then recursively cleanup(wiki, categoryMembers[i]);
			}
			if (categoryMembers[i].startsWith("File:")) {
				WikiPage target = new WikiPage(wiki, categoryMembers[i]);
				target.cleanupOvercat(1);
				// bot==false for now
				target.writeText(false);
			}
		}
		System.out
				.println("Gone through all subcategories and files of [[:Category:"
						+ category + "]]. Exiting.");
	}
}
