package overbot;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.*;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import shared.Wiki;
import shared.WikiPage;

public class OverBot {

	public static void main(String[] args) {

		System.out.println("v13.12.12");

		Wiki commons = new Wiki("commons.wikimedia.org");
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
		login(commons, args[0]);
		cleanupOvercat(commons, args[1]);

	}

	/**
	 * Asks the user to type in his password in the console
	 * 
	 * @param wiki
	 *            The wiki to log into
	 * @param user
	 *            The user to log on
	 */
	private static void login(Wiki wiki, String user) {
		System.out.println("Please type in the password for " + user + ".");
		try {
			try {
				wiki.login(user, System.console().readPassword());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FailedLoginException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 
	 * Remove categories from all items in a given category for a given wiki
	 * 
	 * @param wiki
	 *            Target wiki
	 * @param category
	 *            The category to be cleaned up.
	 * 
	 */
	private static void cleanupOvercat(Wiki wiki, String category) {
		String categoryMembers[] = { "" };

		wiki.setThrottle(8 * 1000);
		wiki.setMaxLag(2); // be nice and stall my bot after 2s of lag (instead
							// of 5s default)

		try {
			categoryMembers = wiki.getCategoryMembers(category);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (int i = 0; i < categoryMembers.length; i++) {

			if (categoryMembers[i].startsWith("Category:")) {
				// may cause infinite loop -> ignore for now
				System.out.println("Found a subcategory " + categoryMembers[i]
						+ " which is ignored due to a possible infinite loop.");
				// cleanupOvercat(wiki, categoryMembers[i]);
			}
			if (categoryMembers[i].startsWith("File:")) {
				try {
					//if(lastEditor==user) continue;
					WikiPage target = new WikiPage(wiki, categoryMembers[i], true);
					target.cleanupWikitext();
					target.cleanupOvercat(1);
					try {
						// bot==false for now
						target.writeText(false);
					} catch (LoginException e1) {
						e1.printStackTrace();
					}
					System.exit(1);
					
					String filename = categoryMembers[i];
					String pageText = wiki.getPageText(filename);

					String parentCats[] = getParentCats(pageText);
					String allGrandparentCats[] = all_grand_parentCats(wiki,
							parentCats);
					String cleanParentCats[] = smallestNOVERCATsubset(
							parentCats, allGrandparentCats);

					if (parentCats.length > cleanParentCats.length) {
						try {
							pageText = cleanupText(pageText);
							String removedCatsWikitext = removedCatsWikitext(
									parentCats, cleanParentCats);
							pageText = removeCatsFromText(parentCats, pageText);
							wiki.edit(
									filename,
									pageText + "\n"
											+ categoryWikiText(cleanParentCats),
									"Bot: Removed "
											+ (parentCats.length - cleanParentCats.length)
											+ " categories which are parent of already present categories (Per [[COM:OVERCAT]]): "
											+ removedCatsWikitext);
						} catch (LoginException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		System.out
				.println("Gone through all subcategories and files of [[:Category:"
						+ category + "]]. Exiting.");

	}

	private static String cleanupText(String pageText) {
		return pageText.replaceAll(
				" *\\[\\[category *: *([^]]*?) *(\\|[^]]*)?\\]\\] *",
				"[[Category:$1$2]]");
	}

	/**
	 * Transforms the String[] object into a clean wiki-text containing the
	 * elements (categories) of the array in a new line each.
	 * 
	 * @param cleanParentCats
	 *            The String[] containing the categories
	 * @return A wiki-text string
	 */
	private static String categoryWikiText(String[] cleanParentCats) {
		String wikit = "";
		for (int i = 0; i < cleanParentCats.length; i++) {
			wikit = wikit + "[[" + cleanParentCats[i] + "]]";
			if (i < cleanParentCats.length - 1)
				wikit = wikit + "\n";
		}
		return wikit;
	}

	/**
	 * Return a String[] object containing all Grand(-Grand(-Grand-)-)-parents
	 * of a given set of parentCats
	 * 
	 * @param wiki
	 *            The wiki to be examined
	 * @param parentCats
	 *            The String[] object containing all parentCats to be used for
	 *            the search of all Grand...
	 * @return The String[] object wrapping all Grand...-parents without having
	 *         duplicate entries
	 */
	private static String[] all_grand_parentCats(Wiki wiki, String[] parentCats) {
		Set<String> listboth = new LinkedHashSet<String>();
		try {
			for (int i = 0; i < parentCats.length; i++) {
				listboth.addAll(Arrays.asList(getParentCats(wiki
						.getPageText(parentCats[i].split("\\|", 2)[0]))));
				// Change to get_All_ParentCats when it is sure that no infinite
				// loop will be created and that the calculation is feasible
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] list1 = listboth.toArray(new String[listboth.size()]);
		for (int i = 0; i < list1.length; i++) {
			list1[i] = list1[i].split("\\|", 2)[0]; // wipe sortkeys
		}
		Set<String> listfin = new LinkedHashSet<String>();
		listfin.addAll(Arrays.asList(list1));
		return listfin.toArray(new String[listfin.size()]);
	}

	/**
	 * Compare two String[] objects and return the "difference" of both. I.e.
	 * the array elements which are not found in both
	 * 
	 * @param parentCats
	 *            The first String[]
	 * @param cleanParentCats
	 *            The second String[]
	 * @return The difference rendered into wiki-text
	 */
	private static String removedCatsWikitext(String[] parentCats,
			String[] cleanParentCats) {
		String removedCats = "";
		for (int r = 0; r < parentCats.length; r++) {
			boolean found = false;
			for (int r2 = 0; r2 < cleanParentCats.length; r2++) {
				found = (parentCats[r].equals(cleanParentCats[r2]));
				if (found)
					break;
			}
			if (!found) {
				removedCats = removedCats + "-[["
						+ parentCats[r].split("\\|", 2)[0] + "]] ";
			}
		}
		return removedCats;
	}

	/**
	 * Determines the smallest subset which contains only variables from
	 * parentCats and not from allGrandparentCats
	 * 
	 * @param parentCatsI
	 *            The String array to be cleaned
	 * @param allGrandparentCats
	 *            The String array containing all variables to be removed from
	 *            parentCats
	 * @return The smallest subset
	 */
	private static String[] smallestNOVERCATsubset(String[] parentCats,
			String[] allGrandparentCats) {
		String revoked = "53d42eaaabd5db19671d22c69380edc2";
		String[] parentCatsI = parentCats.clone();
		int revokedC = 0;
		for (int i = 0; i < parentCatsI.length; i++) {
			for (int a = 0; a < allGrandparentCats.length; a++) {
				if (parentCatsI[i].split("\\|", 2)[0]
						.equals(allGrandparentCats[a])) {
					parentCatsI[i] = revoked;
					revokedC++;
				}
			}
		}
		String[] smallestSubset = new String[parentCatsI.length - revokedC];
		int a = 0;
		for (int i = 0; i < parentCatsI.length; i++) {
			if (!(parentCatsI[i].equals(revoked))) {
				smallestSubset[a++] = parentCatsI[i];
			}
		}
		return smallestSubset;
	}

	/**
	 * Removes all Strings found in cats[] from the pageText
	 * 
	 * @param cats
	 *            The string array containing the categories
	 * @param pageText
	 *            The page Text
	 * @return the cleaned page text
	 */
	private static String removeCatsFromText(String[] cats, String pageText) {
		String[] catsI = cats.clone();
		for (int z = 0; z < catsI.length; z++) {
			if (!catsI[z].startsWith("[["))
				catsI[z] = "[[" + catsI[z];
			if (!catsI[z].endsWith("]]"))
				catsI[z] = catsI[z] + "]]";
			// DO NOT REPLACE IN COMMENTS!! <!-- -->
			pageText = replaceIgnoreComments(pageText, catsI[z], "");
			pageText = replaceIgnoreComments(
					replaceIgnoreComments(pageText, "\n\n\n", "\n\n"),
					"\n\n\n", "\n\n");
		}
		return pageText;
	}

	private static String replaceIgnoreComments(String pageText, String target,
			String replacement) {
		String commentPre = "<!--";
		String commentSuf = "-->";
		String split[] = pageText.split(commentPre, 2);
		// tackle stuff in front of comment -> Just replace
		pageText = split[0].replace(target, replacement);
		if (split.length == 2) { // if comment actually found
			// search for the end of the comment
			String split2[] = split[1].split(commentSuf, 2);
			if (split2.length == 2) { // if end of comment actually found
				// keep comment and only replace stuff after the end of the
				// comment recursively
				pageText = pageText + commentPre + split2[0] + commentSuf
						+ replaceIgnoreComments(split2[1], target, replacement);
			} else {
				// no comment found -> Just replace
				pageText = pageText + commentPre
						+ split[1].replace(target, replacement);
			}
		}
		return pageText;
	}

	/**
	 * Extract all categories from the given pageText
	 * 
	 * @param pageText
	 *            The String to be examined
	 * @return all categories in a string[]
	 */
	private static String[] getParentCats(String pageText) {
		Matcher m = Pattern.compile("\\[\\[[cC]ategory:[^}#\\]\\[{><%]*\\]\\]")
				.matcher(wipeComments(pageText));
		// percent seems to be allowed in some cases
		// thus: it should match: [
		// %!\"$&'()*,\\-.\\/0-9:;=?@A-Z\\\\^_`a-z~\\x80-\\xFF+]
		// is this valid regex to match the cats?
		// replaced "<!--(([^-])|(-[^-])|(--[^>]))*-->"
		// by "<!--([^-]?(-[^-])?(--[^>])?)*-->" (used in pageText.replace)
		int length = 0;
		while (m.find()) {
			System.out.println("Category " + ++length + " found: " + m.group());
		}
		m.reset();
		String[] ParentCats = new String[length];
		length = 0;
		while (m.find()) {
			ParentCats[length++] = m.group();
			String[] temp = ("C" + ParentCats[length - 1].substring(3,
					ParentCats[length - 1].length() - 2)).split(":", 2);

			ParentCats[length - 1] = temp[0] + ":"
					+ temp[1].substring(0, 1).toUpperCase()
					+ temp[1].substring(1, temp[1].length());
		}
		return ParentCats;
	}

	private static CharSequence wipeComments(String pageText) {
		String commentPre = "<!--";
		String commentSuf = "-->";
		String[] split = pageText.split(commentPre, 2);
		// Just copy the text in front of the comment
		pageText = split[0];
		if (split.length == 2) { // if comment actually found
			// search for the end of the comment
			String split2[] = split[1].split(commentSuf, 2);
			if (split2.length == 2) { // if end of comment actually found
				// delete comment and only keep stuff after the end of the
				// comment. (Of course after recursively removing the
				// comments from this one as well
				pageText = pageText + wipeComments(split2[1]);
			}
		}
		return pageText;
	}

}
