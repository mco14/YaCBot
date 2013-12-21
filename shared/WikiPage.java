package shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class WikiPage {
	private boolean isFile;
	private String name;
	private Category[] parents;
	private String text;
	private Wiki wiki;
	private boolean isCleanedup;
	// editSummary stays empty if only minor cleanups were made
	private String editSummary;

	/**
	 * Creates a new object of the class WikiPage. It is possible to clean up
	 * the wikitext of the page
	 * 
	 * @param wiki
	 *            The wiki where the WikiPage is located
	 * @param name
	 *            The name of the page with prefix (e.g. "File:", "Category:",
	 *            ...)
	 */
	public WikiPage(Wiki wiki, String name) throws IOException {
		this.isFile = name.split(":", 2)[0].toLowerCase().equals("file");
		this.wiki = wiki;
		this.name = name;
		this.text = wiki.getPageText(name);
		this.isCleanedup = false;
		this.editSummary = "";
	}

	public String getEditSummary() {
		return editSummary;
	}

	public String getName() {
		return name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void cleanupWikitext() {
		this.isCleanedup = true;
		String caseInsensitive = "(?iu)";
		String caseInsensitiveMultiline = "(?ium)";
		String cleanText;
		if (isFile) {
			cleanText = text
					.replaceAll(
							caseInsensitiveMultiline
									+ "^== *(summary|sumario|descri(ption|pción|ção do arquivo)|achoimriú)( */ *(summary|sumario|descri(ption|pción|ção do arquivo)|achoimriú))? *==",
							"== {{int:filedesc}} ==")
					.replaceAll(
							caseInsensitiveMultiline
									+ "^== *(\\[\\[.*?\\|)?(licen[cs](e|ing|ia)( */ *licen[cs](e|ing|ia))?|\\{\\{int:license\\}\\})(\\]\\])?:? *==",
							"== {{int:license-header}} ==")
					.replaceAll(
							caseInsensitiveMultiline
									+ "^== *(original upload (log|history)|file history|ursprüngliche bild-versionen) *==",
							"== {{original upload log}} ==")
					.replaceAll(
							caseInsensitive
									+ "(\\| *technique *= *)(?:\\{\\{ *(?:en|de) *\\|)? *(?:oil[ -]on[ -]canvas|öl[ -]auf[ -]leinwand) *(?:\\}\\})?(\\||\\}\\}|\\r|\\n)",
							"$1{{technique|oil|canvas}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *technique *= *)\\{\\{ *de *\\| *öl[ -]auf[ -]holz *\\}\\}(\\||\\}\\}|\\r|\\n)",
							"$1{{technique|oil|wood}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *technique *= *)\\{\\{ *de *\\| *öl[ -]auf[ -]eichenholz *\\}\\}(\\||\\}\\}|\\r|\\n)",
							"$1{{technique|oil|panel|wood=oak}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *technique *= *)(?:\\{\\{ *en *\\|)? *oil[ -]on[ -]panel *(?:\\}\\})?(\\||\\}\\}|\\r|\\n)",
							"$1{{technique|oil|panel}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *technique *= *)\\{\\{ *de *\\| *aquarell *\\}\\}(\\||\\}\\}|\\r|\\n)",
							"$1{{technique|watercolor}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *technique *= *)\\{\\{ *de *\\| *fresko *\\}\\}(\\||\\}\\}|\\r|\\n)",
							"$1{{technique|fresco}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *(?:author|artist) *= *)(?:unknown|\\{\\{unknown\\}\\}|\\?+)\\.? *(\\||\\}\\}|\\r|\\n)",
							"$1{{unknown|author}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *source *= *)(?:own work)? *(?:-|;|</?br *[/\\\\]?>)? *(?:own(?: work(?: by uploader)?)?|(?:œuvre |travail )?personnel(?:le)?|self[- ]made|création perso|selbst fotografiert|obra pr[òo]pia|trabajo propr?io) *(?:\\(own work\\))?\\.? *(\\||\\}\\}|\\r|\\n)",
							"$1{{own}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *source *= *)(?:\\{\\{[a-z]{2,3} *\\|)? *(?:own(?: work(?: by uploader)?)?|travail personnel|self[- ]made|création perso|selbst fotografiert|obra pr[òo]pia|trabajo propr?io) *(?:\\}\\})? *(?:\\{\\{[a-z]{2,3} *\\|)? *(?:\\(?(?:own *work)\\)?)? *(?:\\}\\})?(\\||\\}\\}|\\r|\\n)",
							"$1{{own}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *author *= *)(?:anonym(?:e|ous)?|anonyymi|anoniem|an[oòóô]nimo?|ismeretlen|不明（匿名）|미상|ανώνυμος|аноним(?:ен|ный художник)|neznámy|nieznany)\\.? *(\\||\\}\\}|\\r|\\n)",
							"$1{{anonymous}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *gallery *= *)private(?: collection)? *(\\||\\}\\}|\\r|\\n)",
							"$1{{private collection}}$2");
			cleanText = multipleReplaceAll(cleanText, caseInsensitiveMultiline
					+ "^ *== *(.*?) *== *[\\r\\n]+ *== *\\1 *== *$", "== $1 ==");
			if (!(text.equals(cleanText))) {
				this.editSummary = editSummary
						+ "[[Com:IntRegex|Internationalisation]]. ";
				this.text = cleanText;
			}
			cleanText = text
					.replaceAll(
							caseInsensitive
									+ "(\\| *description *=) *(?:\\{\\{ *description missing *\\}\\}|(?:\\{\\{en *\\|)? *(?:'')?no original description(?:'')? *(?:\\}\\})?) *(\\||\\}\\}|\\r|\\n)",
							"$1$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *permission *=) *(?:-|see(?: licens(?:e|ing))?(?: below)?|yes|oui)\\.? *(\\||\\}\\}|\\r|\\n)",
							"$1$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *other[_ ]versions *=) *(?:<i>)?(?:-|no|none?(?: known)?)\\.?(?:</i>)? *(\\||\\}\\}|\\r|\\n)",
							"$1$2");
			if (!(text.equals(cleanText))) {
				this.editSummary = editSummary
						+ "Removing redundant information. ";
				// per
				// https://commons.wikimedia.org/wiki/Commons:IntRegex#.7B.7BInformation.7D.7D_fields
				this.text = cleanText;
			}
			cleanText = text
					.replaceAll(
							caseInsensitive
									+ "(\\| *date *= *)(?:created|made|taken)? *([0-9]{4})(-| |/|\\.|)(0[1-9]|1[0-2])\\3(1[3-9]|2[0-9]|3[01])(\\||\\}\\}|\\r|\\n)",
							"$1$2-$4-$5$6")
					.replaceAll(
							caseInsensitive
									+ "(\\| *date *= *)(?:created|made|taken)? *([0-9]{4})(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3(0[1-9]|1[0-2])(\\||\\}\\}|\\r|\\n)",
							"$1$2-$5-$4$6")
					.replaceAll(
							caseInsensitive
									+ "(\\| *date *= *)(?:created|made|taken)? *(0[1-9]|1[0-2])(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3([0-9]{4})(\\||\\}\\}|\\r|\\n)",
							"$1$5-$2-$4$6")
					.replaceAll(
							caseInsensitive
									+ "(\\| *date *= *)(?:created|made|taken)? *(1[3-9]|2[0-9]|3[01])(-| |/|\\.|)(0[1-9]|1[0-2])\\3(2[0-9]{3}|1[89][0-9]{2})(\\||\\}\\}|\\r|\\n)",
							"$1$5-$4-$2$6")

					.replaceAll(
							caseInsensitive
									+ "(\\| *date *= *)(?:created|made|taken)? *\\{\\{date\\|([0-9]{4})\\|(0[1-9]|1[012])\\|(0?[1-9]|1[0-9]|2[0-9]|3[01])\\}\\}(\\||\\}\\}|\\r|\\n)",
							"$1$2-$3-$4$5")
					.replaceAll(
							caseInsensitive
									+ "(\\| *(?:date|year) *= *)(\\d\\d?)(?:st|nd|rd|th) *century *(\\||\\}\\}|\\r|\\n)",
							"$1{{other date|century|$2}}$3")

					.replaceAll(
							caseInsensitive
									+ "(\\| *(?:date|year) *= *)(?:cir)?ca?\\.? *(\\d{4}) *(\\||\\}\\}|\\r|\\n)",
							"$1{{other date|~|$2}}$3")
					.replaceAll(
							caseInsensitive
									+ "(\\| *(?:date|year) *= *)(?:unknown|\\?+)\\.? *(\\||\\}\\}|\\r|\\n)",
							"$1{{other date|?}}$2")
					.replaceAll(
							caseInsensitive
									+ "(\\| *date *= *)(?:\\{\\{date\\|)?([0-9]{4})[|-](0[1-9]|1[012])[|-](0[1-9]|1[0-9]|2[0-9]|3[01])(?:\\}\\})? *\\((original upload date|according to EXIF data)\\) *(\\||\\}\\}|\\r|\\n)",
							"$1{{$5|$2-$3-$4}}$6");
			if (!(text.equals(cleanText))) {
				this.editSummary = editSummary
						+ "[[Com:Regex#Dates|Standardizing dates]]. ";
				this.text = cleanText;
			}
			cleanText = text
					.replaceAll(
							caseInsensitive
									+ "(<!--)? *\\{\\{ImageUpload\\|(full|basic)\\}\\} *(-->)? *\\n?",
							"")
					.replaceAll(
							caseInsensitive
									+ " *<!-- *Remove this line once you have added categories *--> *",
							"")
					.replaceAll(
							caseInsensitive + " *<!-- *categories *--> *\\n?",
							"").replaceAll("__ *NOTOC *__", "")
			// This is commented out due to the fact stated at
			// https://commons.wikimedia.org/wiki/Commons:Regex#Junk_cleanup
			// .replaceAll(caseInsensitive+"\\{\\{en *(?:\\| *1=)? *\\}\\} *(\\||\\}\\}|\\r|\\n)","$1")
			;
			this.text = cleanText;
			// only minor cleanup per
			// https://commons.wikimedia.org/wiki/Commons:Regex#Junk_cleanup
		}
		// only minor cleanup per
		// https://commons.wikimedia.org/wiki/Commons:Regex#Formatting
		this.text = text.replaceAll("\\n{3,}", "\n\n");
		cleanText = text
				.replaceAll(
						caseInsensitive
								+ "\\[https?://([a-z0-9\\-]{2,3})\\.(?:(w)ikipedia|(wikt)ionary|wiki(n)ews|wiki(b)ooks|wiki(q)uote|wiki(s)ource|wiki(v)ersity|wiki(voy)age)\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
						"[[$2$3$4$5$6$7$8:$1:$9|$10]]")
				.replaceAll(
						caseInsensitive
								+ "\\[https?://(?:(m)eta|(incubator)|(quality))\\.wikimedia\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
						"[[$1$2$3:$4|$5]]")
				.replaceAll(
						caseInsensitive
								+ "\\[https?://commons\\.wikimedia\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
						"[[:$1|$2]]");
		if (!(text.equals(cleanText))) {
			this.editSummary = editSummary
					+ "[[Com:Regex#Links|Standardizing interwikilinks]]. ";
			this.text = cleanText;
		}
		cleanText = text.replaceAll(
				caseInsensitive
						+ " *\\[\\[category *: *([^]]*?) *(\\|[^]]*)?\\]\\] *",
				"[[Category:$1$2]]").replaceAll(
				caseInsensitive
						+ "\\[\\[category: *\\]\\](?:\\n( *\\[\\[category:))?",
				"$1");
		cleanText = multipleReplaceAll(cleanText, caseInsensitive
				+ "\\[\\[category:([^]]+)\\]\\] *\\[\\[category:([^]]+)\\]\\]",
				"[[Category:$1]]\n[[Category:$2]]");
		cleanText = multipleReplaceAll(cleanText, caseInsensitive
				+ "(\\[\\[category:)([^]]+\\]\\])(.*?)\\1\\2\\n?", "$1$2$3");
		if (!(text.equals(cleanText))) {
			this.editSummary = editSummary
					+ "[[Com:Regex#Categories|Category-cleanup]]. ";
			this.text = cleanText;
		}
		this.text = multipleReplaceAll(text, caseInsensitive
				+ "(\\[\\[category:[^]]+\\]\\]\\n)\\n+(\\[\\[category:)",
				"$1$2");
		cleanText = text
				.replaceAll(caseInsensitive + "</?br( )?(/)?\\\\?>", "<br$1$2>")
				.replaceAll(
						caseInsensitive
								+ "(\\{\\{\\}\\}|\\[\\[\\]\\]|<gallery></gallery>|\\[\\[:?File *: *\\]\\]|)",
						"");
		if (!(text.equals(cleanText))) {
			this.editSummary = editSummary
					+ "[[Com:Regex#Formatting|Format-cleanup]]. ";
			this.text = cleanText;
		}
	}

	/**
	 * Make the first char of the string upper-case
	 * 
	 * @param string
	 *            The string to be tackled
	 * @return The string with the first char upper-cased
	 */
	public static String firstCharToUpperCase(String string) {
		if (string == null || string.equals(""))
			return string;
		return Character.toUpperCase(string.charAt(0))
				+ (string.length() > 1 ? string.substring(1) : "");
	}

	/**
	 * Replace all matches of the regex in the string until no change to the
	 * string can be made anymore
	 * 
	 * @param text
	 *            The text to be considered
	 * @param regex
	 *            The regex pattern
	 * @param replacement
	 *            All matches of the regex are replaced by this
	 * @return The string with all matches replaced
	 */
	public static String multipleReplaceAll(String text, String regex,
			String replacement) {
		String string2;
		int maximumReplacements = 1000;
		while (true) {
			string2 = text.replaceAll(regex, replacement);
			{// TODO Remove this after debug!
				if (maximumReplacements == 1) {
					System.out.println("Too many replacements for regex=\n'"
							+ regex + "'\nand replacement=\n'" + replacement
							+ "'\nCurrent text was \n'" + text + "'.");
					System.exit(-1);
				}
			}
			if (string2.equals(text) || maximumReplacements-- == 1)
				return string2;
			else
				text = string2;
		}
	}

	/**
	 * Clean up the [[com:OVERCAT]]-problem for the file
	 * 
	 * @param depth
	 *            The depth which the category tree should be examined. WARNING:
	 *            Set to "1" if you are unsure about possible loops in the tree
	 *            which will most likely cause unexpected behavior
	 * @param ignoreHidden
	 *            If hidden categories should be ignored during the search
	 * @throws IOException
	 */
	public void cleanupOvercat(int depth, boolean ignoreHidden)
			throws IOException {
		if (!isCleanedup)
			this.cleanupWikitext();
		// Category array derived from the pageText
		Category[] parentCategories = getParentCatsNoDupes();

		// String array returned via the API, no duplicate entries, no sortkey,
		// no prefix
		String[] allGrandparentCategories;
		{
			Set<String> listSet = all_grand_parentCats(wiki,
					parentCats(wiki, name, false, ignoreHidden), depth,
					ignoreHidden);
			allGrandparentCategories = listSet.toArray(new String[listSet
					.size()]);
		}
		Object[] cleanedCatsAndText = returnCleanedCatsAndText(
				!editSummary.isEmpty(), parentCategories,
				allGrandparentCategories);
		Category[] cleanParentCategories = (Category[]) cleanedCatsAndText[0];
		String removedCategoriesWikitext = (String) cleanedCatsAndText[1];
		String cleanCategoryWikitext = (String) cleanedCatsAndText[2];

		// Removes the categories from the text while ignoring the content of
		// comments (between "<!--" and "-->")
		String text = getText();
		for (Category z : parentCategories)
			text = replaceAllIgnoreComments(text, "(?iu)" + "\\[\\[Category:"
					+ "\\Q" + z.getName() + "\\E" + "(\\|[^}#\\]\\[{><]*)?"
					+ "\\]\\]", "");

		this.setText((text + cleanCategoryWikitext).replaceAll("\\n{3,}",
				"\n\n"));
		this.parents = cleanParentCategories;
		int numberOfRemovedCategories = parentCategories.length
				- cleanParentCategories.length;
		if (numberOfRemovedCategories > 0)
			this.editSummary = "Removed "
					+ numberOfRemovedCategories
					+ " categories which are [[COM:OVERCAT|parent]] of already present categories: "
					+ removedCategoriesWikitext + ". " + this.getEditSummary();
	}

	/**
	 * Calculate an array of "clean" parent-categories with their sortkeys and
	 * two wiki-code-texts: The removed categories (used for the editSummary)
	 * and the wiki-code representation of the "clean" parent-categories
	 * 
	 * @param cleanupAnyway
	 *            Whether to clean up regardless of the number of removed
	 *            categories or not
	 * @param parentCategories
	 *            The (not yet clean) parent-categories
	 * @param grandparentStrings
	 *            The previously determined categories which are supposed to be
	 *            the grandparent categories
	 * @return The three items bundled into a JAVA-Object array
	 */
	private static Object[] returnCleanedCatsAndText(boolean cleanupAnyway,
			Category[] parentCategories, String[] grandparentStrings) {
		Category[] cleanCategories = new Category[parentCategories.length];
		String categoryWikitext = "";
		String removedCatsWikitext = "";

		int revokedCounter = 0;
		String revokedFlag = "e7db5f37c0a2bc9b525d8ab86ea9ed12";
		// calculate the number of redundant categories
		// TODO: reduce to one for loop...
		for (int i = 0; i < parentCategories.length; i++) {
			cleanCategories[i] = new Category(parentCategories[i].getName(),
					parentCategories[i].getSortkey()); // clone
			for (int r = 0; r < grandparentStrings.length; r++) {
				if ((parentCategories[i].getName()
						.equals(grandparentStrings[r]))) {
					removedCatsWikitext = removedCatsWikitext + "[[Category:"
							+ parentCategories[i].getName() + "]] ";
					revokedCounter++;
					cleanCategories[i].setName(revokedFlag);
					break;
				}
			}
		}
		// create a new array for the clean categories taking into account the
		// number of redundant categories
		if (cleanupAnyway || revokedCounter > 0) {
			Category[] cleanCategoriesReturn = new Category[cleanCategories.length
					- revokedCounter];
			int temp = 0;
			for (Category i : cleanCategories) {
				if (!i.getName().equals(revokedFlag)) {
					cleanCategoriesReturn[temp++] = i;
					categoryWikitext = categoryWikitext
							+ "\n[[Category:"
							+ i.getName()
							+ ((i.getSortkey() == null) ? "]]" : "|"
									+ i.getSortkey() + "]]");
				}
			}
			if (revokedCounter > 0) {
				removedCatsWikitext = removedCatsWikitext.substring(0,
						removedCatsWikitext.length() - 1);
				cleanCategories = cleanCategoriesReturn;
			}
		}
		return new Object[] { cleanCategories, removedCatsWikitext,
				categoryWikitext };
	}

	/**
	 * Replace all matches of the regex with the replacement string in the text
	 * string
	 * 
	 * @param text
	 *            The text to be altered
	 * @param regex
	 *            The regex pattern
	 * @param replacement
	 *            All matches get substituted by this
	 * @return
	 */
	public static String replaceAllIgnoreComments(String text, String regex,
			String replacement) {
		String commentPre = "<!--";
		String commentSuf = "-->";
		String split[] = text.split(commentPre, 2);
		// tackle stuff in front of comment -> Just replace
		text = split[0].replaceAll(regex, replacement);
		if (split.length == 2) {
			// if comment actually found
			// search for the end of the comment
			String split2[] = split[1].split(commentSuf, 2);
			if (split2.length == 2) {
				// if end of comment actually found
				// keep comment and only replace stuff after the end of the
				// comment recursively
				return text
						+ commentPre
						+ split2[0]
						+ commentSuf
						+ replaceAllIgnoreComments(split2[1], regex,
								replacement);
			}
			// no comment found -> Just replace
			return text + commentPre + split[1].replaceAll(regex, replacement);

		}
		return text;
	}

	/**
	 * A static method which calls itself in a recursive manner to create a
	 * string-set of all grandparent categories
	 * 
	 * @param wiki
	 *            The wiki to connect to
	 * @param categories
	 *            The categories to be evaluated by the method (Must not include
	 *            the "Category:" prefix)
	 * @param depth
	 *            The depth to be examined (depth == 1 means no recursion at
	 *            all)
	 * @param ignoreHidden
	 *            If hidden categories should not be considered during search
	 * @return The string-set which was recursively generated (Contains only the
	 *         Category name, no prefix, no sortkey, no duplicate entries)
	 * @throws IOException
	 */
	private static Set<String> all_grand_parentCats(Wiki wiki,
			String[] categories, int depth, boolean ignoreHidden)
			throws IOException {
		if (depth <= 0) {
			Set<String> emptySet = new LinkedHashSet<String>();
			return emptySet;
		}
		Set<String> subSet = new LinkedHashSet<String>();
		for (String cat : categories) {
			String[] tempGrandparent = WikiPage.parentCats(wiki, "Category:"
					+ cat, false, ignoreHidden);
			subSet.addAll(all_grand_parentCats(wiki, tempGrandparent,
					depth - 1, ignoreHidden));
			subSet.addAll(Arrays.asList(tempGrandparent));
		}
		return subSet;
	}

	/**
	 * Return the parent categories of the WikiPage derived from the pageText
	 * 
	 * @return The Category array with no duplicate entries
	 * @throws IOException
	 */
	private Category[] getParentCatsNoDupes() throws IOException {
		if (isCleanedup == false)
			this.cleanupWikitext();
		if (parents == null) {
			String[] parentCats = parentCatsFromPagetext(wiki, text, true);
			// wipe dupes
			Set<String> names = new HashSet<String>();
			List<Category> catList = new ArrayList<Category>();
			for (String name : parentCats) {
				if (names.add(name)) {
					String splitString[] = name.split("\\|", 2);
					catList.add(new Category(splitString[0],
							(splitString.length == 2) ? splitString[1] : null));
				}
			}
			this.parents = catList.toArray(new Category[catList.size()]);
		}
		return parents;
	}

	/**
	 * Returns the parent categories via the API
	 * 
	 * @param wiki
	 *            The wiki to connect to
	 * @param title
	 *            The title of the page to be evaluated
	 * @param sortkey
	 *            If the sortkey should be included into the string array
	 * @param ignoreHidden
	 *            If hidden categories should be ignored during the search
	 * @return A string array containing all categories and (if desired) the
	 *         sortkey separated by "|". (The "Category:" prefix string is
	 *         wiped!)
	 * @throws IOException
	 */
	private static String[] parentCats(Wiki wiki, String title,
			boolean sortkey, boolean ignoreHidden) throws IOException {
		String[] temp = wiki.getCategories(title, sortkey, ignoreHidden);
		for (int t = 0; t < temp.length; t++)
			// remove the "Category:" prefix string
			temp[t] = temp[t].split(":", 2)[1];
		return temp;
	}

	/**
	 * Returns the parent categories which can be inferred from the given text
	 * 
	 * @param wiki
	 *            The wiki to connect to
	 * @param text
	 *            The text to be evaluated (Commented content gets ignored)
	 * @param sortkey
	 *            If the sortkey should be included into the string array
	 * @return A string array containing all category names (no "Category:"
	 *         prefix) and (if desired) the sortkey separated by "|"
	 */
	public static String[] parentCatsFromPagetext(Wiki wiki, String text,
			boolean sortkey) {
		Matcher m = Pattern
				.compile(
						"\\[\\[[cC]ategory:[^\\|#\\]\\[}{><]+(\\|[^#\\]\\[}{><]*)?\\]\\]")
				.matcher(wipeComments(text));
		{// TODO Remove this after debug!
			// The above match (if any) should also match the following regex to
			// be valid
			// [%!\"$&'()*,\\-.\\/0-9:;=?@A-Z\\\\^_`a-z~\\x80-\\xFF+]
		}
		int hits = 0;
		List<String> parentsList = new ArrayList<String>();
		while (m.find()) {
			System.out.println("Category " + ++hits + " found in page-text: "
					+ m.group());
			if (sortkey == false) {
				// Only the name of the category
				parentsList.add(WikiPage.firstCharToUpperCase(m.group()
						.substring(2, m.group().length() - 2).split(":", 2)[1]
						.split("\\|", 2)[0]));
			} else {
				// The name and the sortkey (if existent)
				parentsList
						.add(WikiPage.firstCharToUpperCase(m.group()
								.substring(2, m.group().length() - 2)
								.split(":", 2)[1]));
			}
		}
		return parentsList.toArray(new String[parentsList.size()]);
	}

	/**
	 * Return the text with all comments removed
	 * 
	 * @param text
	 *            The text to be altered
	 * @return The altered text which lacks all comments
	 */
	public static String wipeComments(String text) {
		String commentPre = "<!--";
		String commentSuf = "-->";
		String[] split = text.split(commentPre, 2);
		// Just copy the text in front of the comment
		text = split[0];
		if (split.length == 2) { // if comment actually found
			// search for the end of the comment
			String split2[] = split[1].split(commentSuf, 2);
			if (split2.length == 2) { // if end of comment actually found
				// delete comment and only keep stuff after the end of the
				// comment. (Of course after recursively removing the
				// comments from this one as well
				text = text + wipeComments(split2[1]);
			}
		}
		return text;
	}

	/**
	 * Write the text of the WikiPage to the wiki if any relevant changes were
	 * made
	 * 
	 * @throws LoginException
	 * @throws IOException
	 */
	public void writeText() throws LoginException, IOException {
		if (this.getEditSummary().length() == 0)
			return;
		wiki.edit(this.getName(), this.getText(),
				"Bot: " + this.getEditSummary());
		this.editSummary = "";
		this.isCleanedup = false;
	}
}

class Category {
	private String name;
	private String sortkey;

	/**
	 * Internal representation of a category
	 * 
	 * @param name
	 *            The name of the category (without the "Category:"-prefix)
	 * @param sortkey
	 *            Either a String holding the sortkey or null
	 */
	Category(String name, String sortkey) {
		this.name = WikiPage.firstCharToUpperCase(name);
		this.sortkey = sortkey;
	}

	String getName() {
		return name;
	}

	String getSortkey() {
		return sortkey;
	}

	void setSortkey(String sortkey) {
		this.sortkey = sortkey;
	}

	void setName(String name) {
		this.name = name;
	}
}