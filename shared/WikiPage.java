package shared;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

class Category {
	private String name;
	private String sortkey;

	public Category(String name, String sortkey) {
		this.name = WikiPage.firstCharToUpperCase(name);
		this.sortkey = sortkey;
	}

	public String getName() {
		return name;
	}

	public String getSortkey() {
		return sortkey;
	}

	public void setSortkey(String sortkey) {
		this.sortkey = sortkey;
	}

	public void setName(String name) {
		this.name = name;
	}
}

public class WikiPage {
	private boolean isFile;
	private String name;
	private Category[] parents;
	private String[] grandparents;
	private Category[] cleanparents;
	private String text;
	private Wiki wiki;
	private boolean isCleanedup;
	// stays empty if only minor cleanups were made
	private String editSummary = "";

	public WikiPage(Wiki wiki, String name, boolean isFile) throws IOException {
		this.isFile = isFile;
		this.wiki = wiki;
		this.name = name;
		this.text = wiki.getPageText(name);
		this.isCleanedup = false;
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
									+ "(\\| *(?:author|artist) *= *)(?:unknown|\\?+)\\.? *(\\||\\}\\}|\\r|\\n)",
							"$1{{unknown}}$2")
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
			// only minor cleanup per [1] and [2]
			// [1] https://commons.wikimedia.org/wiki/Commons:Regex#Junk_cleanup
			// [2] https://commons.wikimedia.org/wiki/Commons:Regex#Formatting
		}
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
		cleanText = text
				.replaceAll(
						caseInsensitive
								+ " *\\[\\[category *: *([^]]*?) *(\\|[^]]*)?\\]\\] *",
						"[[Category:$1$2]]")
				.replaceAll(
						caseInsensitive
								+ "\\[\\[category: *\\]\\](?:\\n( *\\[\\[category:))?",
						"$1")
				.replaceAll(
						caseInsensitive
								+ "(\\[\\[category:[^]]+\\]\\]\\n)\\n+(\\[\\[category:)",
						"$1$2");
		cleanText = multipleReplaceAll(cleanText, caseInsensitive
				+ "\\[\\[category:([^]]+)\\]\\] *\\[\\[category:([^]]+)\\]\\]",
				"[[Category:$1]]\n[[Category:$2]]");
		cleanText = multipleReplaceAll(cleanText, caseInsensitive
				+ "(\\[\\[category:)([^]]+\\]\\])(.*?)\\1\\2\\n?", "$1$2$3");
		if (!(text.equals(cleanText))) {
			this.editSummary = editSummary
					+ "[[Com:Regex#Categories|Category cleanup]]. ";
			this.text = cleanText;
		}
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

	public static String firstCharToUpperCase(String string) {
		if (string.equals("") || string == null)
			return string;
		return Character.toUpperCase(string.charAt(0))
				+ (string.length() > 1 ? string.substring(1) : "");
	}

	private String multipleReplaceAll(String string, String regex,
			String replacement) {
		String string2;
		int maximumReplacements = 1000;
		while (true) {
			string2 = string.replaceAll(regex, replacement);
			if (maximumReplacements == 1) {
				System.out.println("Too many replacements for regex='" + regex
						+ "' and replacement='" + replacement
						+ "'. Text was ~'" + string + "'");
				System.exit(-1);
			}
			if (string2.equals(string) || maximumReplacements-- == 0)
				return string2;
			else
				string = string2;
		}
	}

	public void cleanupOvercat(int depth) throws IOException {
		Category[] parentCategories = this.getParentCats();
		String[] grandparentStrings = this.getAllGrandparentCats(depth);

		Object[] stuff = calculate(parentCategories, grandparentStrings);

		Category[] cleanParentCategories = (Category[]) stuff[0];
		this.parents = cleanParentCategories;
		String[] removedCategories = (String[]) stuff[1];
		String removedCategoriesWikitext = (String) stuff[2];
		String categoryWikitext = (String) stuff[3];
		if (cleanParentCategories.length < parentCategories.length) {
			this.setText(removeCatsFromText(parentCategories, this.getText())
					+ categoryWikitext);
			this.editSummary = "Removed "
					+ (parentCategories.length - cleanParentCategories.length)
					+ " categories which are parent of already present categories (Per [[COM:OVERCAT]]): "
					+ removedCategoriesWikitext + ". " + this.getEditSummary();
		}
	}

	private Object[] calculate(Category[] parentCategories,
			String[] grandparentStrings) {
		// create: # cleanParentcats with sortkey # removed cats witthout
		// sortkey and # editlinepart
		String revoked = "e7db5f37c0a2bc9b525d8ab86ea9ed12";
		Category[] cleanCategories = parentCategories.clone();
		String[] removedCategories = new String[parentCategories.length];
		String categoryWikitext = "\n";
		int revokedCounter = 0;

		String removedCatsWikitext = "";
		for (int i = 0; i < parentCategories.length; i++) {
			for (int r = 0; r < grandparentStrings.length; r++) {
				if ((parentCategories[i].getName()
						.equals(grandparentStrings[r]))) {
					removedCatsWikitext = removedCatsWikitext + "[[Category:"
							+ parentCategories[i].getName() + "]] ";
					removedCategories[revokedCounter++] = parentCategories[i]
							.getName();
					cleanCategories[i].setName(revoked);
					break;
				}
			}
		}
		Category[] cleanCategoriesReturn = new Category[cleanCategories.length
				- revokedCounter];
		int c = 0;
		String[] removedCategoriesReturn = new String[revokedCounter];
		int r = 0;
		for (int i = 0; i < parentCategories.length; i++) {
			if (cleanCategories[i].getName().equals(revoked))
				removedCategoriesReturn[r++] = removedCategories[i];
			else {
				cleanCategoriesReturn[c++] = cleanCategories[i];
				categoryWikitext = categoryWikitext
						+ "[[Category:"
						+ cleanCategories[i].getName()
						+ ((cleanCategories[i].getSortkey() != null) ? "|"
								+ cleanCategories[i].getSortkey() + "]]" : "]]");
			}
		}
		Object[] returnObjects = new Object[4];
		returnObjects[0] = cleanCategoriesReturn;
		returnObjects[1] = removedCategoriesReturn;
		returnObjects[2] = removedCatsWikitext;
		returnObjects[3] = categoryWikitext;
		return returnObjects;
	}

	private static String removeCatsFromText(Category[] parentCategories,
			String pageText) {
		Category[] catsI = parentCategories.clone();
		for (int z = 0; z < catsI.length; z++) {
			String name = parentCategories[z].getName();
			String regexcaseinsensitive = "";
			if (name.length() >= 1)
				regexcaseinsensitive = "[" + name.substring(0, 1).toLowerCase()
						+ name.substring(0, 1).toUpperCase() + "]";
			if (name.length() >= 2)
				name = name.substring(1);
			else
				name = "";
			pageText = replaceAllIgnoreComments(pageText,"\\[\\[[Cc]ategory:"
					+ regexcaseinsensitive + name, "");
		}
		return pageText;
	}

	private static String replaceAllIgnoreComments(String pageText,
			String target, String replacement) {
		String commentPre = "<!--";
		String commentSuf = "-->";
		String split[] = pageText.split(commentPre, 2);
		// tackle stuff in front of comment -> Just replace
		pageText = split[0].replaceAll(target, replacement);
		if (split.length == 2) { // if comment actually found
			// search for the end of the comment
			String split2[] = split[1].split(commentSuf, 2);
			if (split2.length == 2) { // if end of comment actually found
				// keep comment and only replace stuff after the end of the
				// comment recursively
				pageText = pageText + commentPre + split2[0] + commentSuf
						+ replaceAllIgnoreComments(split2[1], target, replacement);
			} else {
				// no comment found -> Just replace
				pageText = pageText + commentPre
						+ split[1].replaceAll(target, replacement);
			}
		}
		return pageText;
	}

	private String[] getAllGrandparentCats(int depth) throws IOException {

		String[] parentStrings = new String[this.getParentCats().length];
		for (int i = 0; i < this.getParentCats().length; i++) {
			parentStrings[i] = this.getParentCats()[i].getName();
		}
		Set<String> listSet = all_grand_parentCats(wiki, parentStrings, depth);

		this.grandparents = listSet.toArray(new String[listSet.size()]);
		return grandparents;
	}

	public static Set<String> all_grand_parentCats(Wiki wiki,
			String[] categories, int depth) throws IOException {
		if (depth <= 0) {
			Set<String> emptySet = new LinkedHashSet<String>();
			return emptySet;
		}
		Set<String> subSet = new LinkedHashSet<String>();
		for (int i = 0; i < categories.length; i++) {
			String[] parentStringsI = WikiPage.parentCats(wiki,
					wiki.getPageText(categories[i]), false);
			for (int a = 0; a < parentStringsI.length; a++) {
				subSet.addAll(all_grand_parentCats(wiki, parentStringsI,
						depth - 1));
			}
			subSet.addAll(Arrays.asList(parentStringsI));
		}
		return subSet;
	}

	private Category[] getParentCats() {
		if (isCleanedup == false)
			this.cleanupWikitext();
		if (parents == null) {
			String[] parentStrings = parentCats(wiki, text, true);
			Category[] parentCategories = new Category[parentStrings.length];
			for (int i = 0; i < parentStrings.length; i++) {
				String splitString[] = parentStrings[i].split("\\|", 2);
				parentCategories[i] = new Category(splitString[0],
						(splitString.length == 2) ? splitString[1] : null);
			}
			this.parents = parentCategories;
		}
		return parents;
	}

	public static String[] parentCats(Wiki wiki, String text, boolean sortkey) {
		Matcher m = Pattern.compile("\\[\\[[cC]ategory:[^}#\\]\\[{><]*\\]\\]")
				.matcher(wipeComments(text));
		// percent seems to be allowed in some cases
		// thus: it should match:
		// [%!\"$&'()*,\\-.\\/0-9:;=?@A-Z\\\\^_`a-z~\\x80-\\xFF+]
		// is this valid regex to match the cats?
		// @deprecatedFollowing
		// replaced "<!--(([^-])|(-[^-])|(--[^>]))*-->"
		// by "<!--([^-]?(-[^-])?(--[^>])?)*-->" (used in pageText.replace)
		int hits = 0;
		while (m.find()) {
			System.out.println("Category " + ++hits + " found: " + m.group());
		}
		m.reset();
		String[] parents = new String[hits];
		hits = 0;
		while (m.find()) {
			if (sortkey == false) {
				// Only the name of the category
				parents[hits++] = WikiPage.firstCharToUpperCase(m.group()
						.substring(2, m.group().length() - 2).split(":", 2)[1]
						.split("\\|", 2)[0]);
			} else {
				// The name and the sortkey
				parents[hits++] = WikiPage.firstCharToUpperCase(m.group()
						.substring(2, m.group().length() - 2).split(":", 2)[1]);
			}
		}
		return parents;
	}

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

	public void writeText(boolean bot) throws LoginException, IOException {
		if (this.getEditSummary().length() == 0)
			return;
		wiki.setMarkBot(bot);
		wiki.edit(this.getName(), this.getText(),
				"Bot: " + this.getEditSummary());
		this.editSummary = "";
	}
}
