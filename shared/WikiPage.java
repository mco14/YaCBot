package shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class WikiPage {
	private boolean isFile;
	private String name;
	private Category[] parents;
	private String[] text;
	private Wiki wiki;
	private boolean isCleanedup;
	// editSummary stays empty if only minor cleanups were made
	private String editSummary;
	private boolean duplicateCategoryCleanup;

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
		this.setPlainText(wiki.getPageText(name));
		this.isCleanedup = false;
		this.editSummary = "";
	}

	public WikiPage() {
	}

	public String getEditSummary() {
		return editSummary;
	}

	public String getName() {
		return name;
	}

	/**
	 * Calculates the plain Wikitext of the page from the internal
	 * representation
	 * 
	 * @return A single String with the whole wikitext of the page
	 */
	public String getPlainText() {
		String plainText = "";
		for (int t = 1; t < text.length; t += 2)
			plainText += text[t];
		return plainText;
	}

	public void setPlainText(String text) {
		LinkedList<String> tokenizedWikitext = tokenizeWikitext(text);
		this.text = tokenizedWikitext.toArray(new String[tokenizedWikitext
				.size()]);
	}

	public void cleanupWikitext() {
		this.isCleanedup = true;
		String caseInsensitive = "(?iu)";
		String caseInsensitiveMultiline = "(?ium)";
		// Stuff that involves comments to be replaced comes here
		this.setPlainText(getPlainText()
				.replaceAll(
						caseInsensitive
								+ "(<!--)? *\\{\\{ImageUpload\\|(full|basic)\\}\\} *(-->)? *\\n?",
						"")
				.replaceAll(
						caseInsensitive
								+ " *<!-- *Remove this line once you have added categories *--> *",
						"")
				.replaceAll(caseInsensitive + " *<!-- *categories *--> *\\n?",
						""));
		// Stuff that must ignore comments follows
		for (int i = 0; i < text.length; ++i) {
			String cleanText;
			String textPart;
			if (text[i++].equals("true"))
				textPart = text[i];
			else
				continue;

			if (isFile) {
				cleanText = textPart
						.replaceAll(
								caseInsensitiveMultiline
										+ "^(\\=+) *(?:summary|(?:Краткое[ _]+)?описание|Beschreibung\\,[ _]+Quelle|Quelle|Beschreibung|वर्णन|sumario|descri(ption|pción|ção do arquivo)|achoimriú)( */ *(?:summary|(?:Краткое[ _]+)?описание|Beschreibung\\,[ _]+Quelle|Quelle|Beschreibung|वर्णन|sumario|descri(ption|pción|ção do arquivo)|achoimriú))? *\\:? *\\1",
								"$1 {{int:filedesc}} $1")
						.replaceAll(
								caseInsensitiveMultiline
										+ "^(\\=+) *(\\[\\[.*?\\|)?(za(?: +d\\'uso)?|Лицензирование|li[zcs]en[zcs](e|ing|ia)?(?:\\s+information)?( */ *(za(?: +d\\'uso)?|Лицензирование|li[zcs]en[zcs](e|ing|ia)?(?:\\s+information)?))?|\\{\\{int:license\\}\\})(\\]\\])? *\\:? *\\1",
								"$1 {{int:license-header}} $1")
						.replaceAll(
								caseInsensitiveMultiline
										+ "^(\\=+) *(?:original upload ?(log|history)|\\{\\{int:wm\\-license\\-original\\-upload\\-log\\}\\}|file ?history|ursprüngliche bild-versionen) *\\:? *\\1",
								"$1 {{original upload log}} $1")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*technique\\s*=\\s*)\\{\\{ *de *\\|\\s*öl[ -]auf[ -]holz *\\}\\}(\\||\\}\\}|\\r|\\n)",
								"$1{{technique|oil|wood}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*technique\\s*=\\s*)\\{\\{ *de *\\|\\s*öl[ -]auf[ -]eichenholz *\\}\\}(\\||\\}\\}|\\r|\\n)",
								"$1{{technique|oil|panel|wood=oak}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*technique\\s*=\\s*)\\{\\{ *de *\\|\\s*aquarell *\\}\\}(\\||\\}\\}|\\r|\\n)",
								"$1{{technique|watercolor}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*technique\\s*=\\s*)\\{\\{ *de *\\|\\s*fresko *\\}\\}(\\||\\}\\}|\\r|\\n)",
								"$1{{technique|fresco}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*(?:author|artist)\\s*=\\s*)(?:unknown?|\\{\\{\\s*unknown\\s*\\}\\}|\\?+|unkown|αγνωστος|sconosciuto|ignoto|desconocido|inconnu|not known|desconhecido|unbekannt|неизвестно|Не известен|neznana|nieznany|непознат|okänd|sconossùo|未知|ukjent|onbekend|nich kennt|ലഭ്യമല്ല|непознат|نه‌ناسرا|descoñecido|不明|ignoto|óþekktur|tak diketahui|ismeretlen|nepoznat|לא ידוע|ûnbekend|tuntematon|نامعلوم|teadmata|nekonata|άγνωστος|ukendt|neznámý|desconegut|Неизвестен|ned bekannt|غير معروف|невідомий)\\s*?\\;?\\.?\\s*?(\\||\\}\\}|\\r|\\n)",
								"$1{{unknown|author}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*source\\s*=\\s*)(?:own work)?\\s*(?:-|;|</?br *[/\\\\]?>)?\\s*(?:own(?: work(?: by uploader)?)?|(?:œuvre |travail )?personnel(?:le)?|self[- ]made|création perso|selbst fotografiert|obra pr[òo]pia|trabajo propr?io)\\s*?(?:\\(own work\\))?\\.? *(\\||\\}\\}|\\r|\\n)",
								"$1{{own}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*source\\s*=\\s*)(?:own[^a-z]*work|opera[^a-z]*propria|trabajo[^a-z]*propio|travail[^a-z]*personnel|eigenes[^a-z]*werk|eigen[^a-z]*werk|собственная[^a-z]*работа|投稿者自身による作品|自己的作品|praca[^a-z]*pw[łl]asna|Obra(?:[^a-z]*do)?[^a-z]*pr[oó]prio|Treball[^a-z]*propi|Собствена[^a-z]*творба|Vlastní[^a-z]*dílo|Eget[^a-z]*arbejde|Propra[^a-z]*verko|Norberak[^a-z]*egina|عمل[^a-z]*شخصي|اثر[^a-z]*شخصی|자작|अपना[^a-z]*काम|נוצר[^a-z]*על[^a-z]*ידי[^a-z]*מעלה[^a-z]*היצירה|Karya[^a-z]*sendiri|Vlastito[^a-z]*djelo[^a-z]*postavljača|Mano[^a-z]*darbas|A[^a-z]*feltöltő[^a-z]*saját[^a-z]*munkája|Karya[^a-z]*sendiri|Eget[^a-z]*verk|Oper[aă][^a-z]*proprie|Vlastné[^a-z]*dielo|Lastno[^a-z]*delo|Сопствено[^a-z]*дело|Oma[^a-z]*teos|Eget[^a-z]*arbete|Yükleyenin[^a-z]*kendi[^a-z]*çalışması|Власна[^a-z]*робота|Sariling[^a-z]*gawa|eie[^a-z]*werk|сопствено[^a-z]*дело|Eige[^a-z]*arbeid|პირადი[^a-z]*ნამუშევარი)\\;?\\.? *(\\||\\}\\}|\\r|\\n)",
								"$1{{own}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*source\\s*=\\s*)(((?:\\'\\'+)?)([\\\"\\']?)(?:selbst\\W*erstellte?s?|selbst\\W*gezeichnete?s?|self\\W*made|eigene?s?)\\W*(?:arbeit|aufnahme|(?:ph|f)oto(?:gra(?:ph|f)ie)?)?\\.?\\4\\3) *(\\||\\}\\}|\\r|\\n)",
								"$1{{own}} ({{original text|1=$2|nobold=1}})$5")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*source\\s*=\\s*)(?:self[^a-z]*photographed|selbst[^a-z]*(?:aufgenommen|(?:f|ph)otogra(?:f|ph)iert?)|投稿者撮影|投稿者の撮影)\\s*?\\.? *(\\||\\}\\}|\\r|\\n)",
								"$1{{self-photographed}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*author\\s*=\\s*)(?:anonym(?:e|ous)?|anonyymi|anoniem|an[oòóô]n[yi]mo?|ismeretlen|不明（匿名）|미상|ανώνυμος|аноним(?:ен|ный художник)|neznámy|nieznany|مجهول|Ананім|Anonymní|Ezezaguna|Anonüümne|אלמוני|អនាមិក|Anonimas|അജ്ഞാതം|Анонимный автор|佚名)\\s*?\\.?\\;?\\s*?(\\||\\}\\}|\\r|\\n)",
								"$1{{anonymous}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*author\\s*=\\s*)(?:unknown\\s*photographer|photographer\\s*unknown)\\s*?\\;?\\.?\\s*?(\\||\\}\\}|\\r|\\n)",
								"$1{{unknown photographer}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*gallery\\s*=\\s*)private(?: collection)? *(\\||\\}\\}|\\r|\\n)",
								"$1{{private collection}}$2")
						.replaceAll(
								caseInsensitive
										+ "The original description page (?:is\\/was|is|was) \\[(?:https?:)?\\/\\/(?:www\\.)?((?:[a-z\\-]+\\.)?wik[a-z]+(?:\\-old)?)\\.org\\/w((?:\\/shared)?)\\/index\\.php\\?title\\=(?:[a-z]+)(?:\\:|%3A)([^\\[\\]\\|}{]+?) +here(?:\\]\\.?|\\.?\\])(\\s+All following user names refer to (?:\\1(?:\\.org)?\\2|(?:wts|shared)\\.oldwikivoyage)\\.?)?",
								"{{original description page|$1$2|$3}}")
						.replaceAll(
								caseInsensitive
										+ "This file was originally uploaded at ([a-z\\-]+\\.wik[a-z]+) as \\[(?:https?:)?\\/\\/\\1\\.org\\/wiki\\/(?:[a-z]+)(?:\\:|%3A)([\\w\\%\\-\\.\\~\\:\\/\\?\\#\\[\\]\\@\\!\\$\\&\\'\\(\\)\\*\\+\\,\\;\\=]+?)(?: |\\])+[^\\]\\n]*\\](?:\\s*\\,?\\s*before it was transferr?ed to commons)?\\.?",
								"{{original description page|$1|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\=+\\s*\\{\\{ *original[ _]+upload[ _]+log *\\}\\}\\s*\\=+\\s*)(\\{\\{ *original[ _]+description[ _]+page *\\|\\s*([a-z\\-]+\\.w[a-z]+)\\s*\\|\\s*[^}\\|\\[{]+\\}\\})\\s*using\\s*\\[\\[\\:en\\:WP\\:FTCG\\|FtCG\\]\\]\\.?",
								"$1{{transferred from|$3||[[:en:WP:FTCG|FtCG]]}} $2");
				cleanText = multipleReplaceAll(
						cleanText,
						caseInsensitiveMultiline
								+ "^ *(\\=+) *(.*?) *\\=+ *[\\r\\n]+\\=+ *\\2 *\\=+ *$",
						"$1 $2 $1");
				if (!(textPart.equals(cleanText))) {
					if (!editSummary
							.contains("[[Com:IntRegex|Internationalisation]]. "))
						this.editSummary = editSummary
								+ "[[Com:IntRegex|Internationalisation]]. ";
					textPart = cleanText;
				}
				cleanText = textPart
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*description\\s*=)\\s*(?:\\{\\{ *description missing *\\}\\}|\\s*description missing\\s*?|(?:\\{\\{en *\\|) *(?:'')?no original description(?:'')? *(?:\\}\\})|(?:'')?no original description(?:'')? *) *(\\||\\}\\}|\\r|\\n)",
								"$1$2")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*permission\\s*=)\\s*((?:\\'\\')?)(?:-|下記を参照|see(?: licens(?:e|ing|e +section))?(?: below)?|yes|oui)\\s*?\\,?\\.?;?\\s*?\\2\\s*?(\\||\\}\\}|\\r|\\n)",
								"$1$3")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*other[_ ]versions\\s*=)\\s*(?:<i>)?(?:-|no|none?(?: known)?|nein|yes|keine|\\-+)\\.?(?:</i>)? *(\\||\\}\\}|\\r|\\n)",
								"$1$2")
						.replaceAll(
								caseInsensitive
										+ "(?:move approved by: *\\[\\[:?User:[^\\]\\[{}]*\\]\\]\\.?)?((?:.|\\n)*?)(?:This image was moved from *\\[\\[:?(?:File|image):?[^\\]\\[{}]*\\]\\]\\.?)?",
								"$1")
						.replaceAll(
								caseInsensitive
										+ "\\{\\{\\s*(?:Ship|Art\\.|bots|football[ _]+kit|template[ _]+other|s|tl|tlxs|template|template[ _]+link|temp|tls|tlx|tl1|tlp|tlsx|tlsp|mbox|tmbox(?:\\/core)?|lan|jULIANDAY|file[ _]+title|nowrap|plural|time[ _]+ago|time[ _]+ago\\/core|toolbar|red|green|sp|other date|max|max\\/2|str[ _]+left|str[ _]+right|music|date|cite[ _]+book|citation\\/core|citation\\/make[ _]+link|citation\\/identifier|citation|cite|cite[ _]+book|citation\\/authors|citation\\/make[ _]+link|cite[ _]+journal|cite[ _]+patent|cite[ _]+web|hide in print|only in print|parmPart|error|crediti|fontcolor|transclude|trim|navbox|navbar|section[ _]+link|yesno|center|unused|•|infobox\\/row)\\s*\\}\\}",
								"")
						.replaceAll(
								caseInsensitive
										+ "\\{\\{\\s*PermissionOTRS\\s*\\|\\s*(?:https?:)?\\/\\/ticket\\.wikimedia\\.org\\/otrs\\/index\\.pl\\?Action\\s*\\=\\s*AgentTicketZoom&(?:amp;)?TicketNumber\\=(\\d+)\\s*\\}\\}",
								"{{PermissionOTRS|id=$1}}");
				if (!(textPart.equals(cleanText))) {
					if (!editSummary
							.contains("Removing redundant information. "))
						this.editSummary = editSummary
								+ "Removing redundant information. ";
					// per
					// https://commons.wikimedia.org/wiki/Commons:IntRegex#.7B.7BInformation.7D.7D_fields
					textPart = cleanText;
				}
				cleanText = textPart
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *([0-9]{4})(-| |/|\\.|)(0[1-9]|1[0-2])\\3(1[3-9]|2[0-9]|3[01])(\\||\\}\\}|\\r|\\n)",
								"$1$2-$4-$5$6")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *([0-9]{4})(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3(0[1-9]|1[0-2])(\\||\\}\\}|\\r|\\n)",
								"$1$2-$5-$4$6")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(0[1-9]|1[0-2])(-| |/|\\.|)(1[3-9]|2[0-9]|3[01])\\3([0-9]{4})(\\||\\}\\}|\\r|\\n)",
								"$1$5-$2-$4$6")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *(1[3-9]|2[0-9]|3[01])(-| |/|\\.|)(0[1-9]|1[0-2])\\3(2[0-9]{3}|1[89][0-9]{2})(\\||\\}\\}|\\r|\\n)",
								"$1$5-$4-$2$6")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:created|made|taken)? *\\{\\{date\\|([0-9]{4})\\|(0[1-9]|1[012])\\|(0?[1-9]|1[0-9]|2[0-9]|3[01])\\}\\}(\\||\\}\\}|\\r|\\n)",
								"$1$2-$3-$4$5")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*(?:date|year)\\s*=\\s*)(?:unknown?(?:\\s*date)?|\\?|unbekannte?s?(\\s*datum)?)",
								"$1{{unknown|date}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*(?:date|year)\\s*=\\s*)(\\d\\d?)(?:st|nd|rd|th) *century *(\\||\\}\\}|\\r|\\n)",
								"$1{{other date|century|$2}}$3")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*(?:date|year)\\s*=\\s*)(?:cir)?ca?\\.? *(\\d{4}) *(\\||\\}\\}|\\r|\\n)",
								"$1{{other date|~|$2}}$3")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*(?:date|year)\\s*=\\s*)(?:unknown|\\?+)\\.? *(\\||\\}\\}|\\r|\\n)",
								"$1{{other date|?}}$2")
						.replaceAll(
								caseInsensitive
										+ "(\\{\\{original upload date\\|\\d{4}\\-\\d{2}\\-\\d{2}\\}\\})\\s*(?:\\(original\\s*upload\\s*date\\)|\\(\\s*first\\s*version\\s*\\);?\\s*\\{\\{original upload date\\|\\d{4}\\-\\d{2}\\-\\d{2}\\}\\}\\s*\\(\\s*last\\s*version\\s*\\))",
								"$1")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:\\{\\{date\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\}\\}|(\\d{4})\\-(\\d{2})\\-(\\d{2}))\\s*\\(\\s*(original upload date|according to EXIF data)\\s*\\)\\s*?(\\||\\}\\}|\\r|\\n)",
								"$1{{$8|$2$5-$3$6-$4$7}}$9")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)\\{\\{\\s*date\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*(\\d+)\\s*\\}\\}\\s*\\(\\s*first\\s*version\\s*\\)\\;?\\s*\\{\\{\\s*date\\s*\\|\\s*\\d+\\s*\\|\\s*\\d+\\s*\\|\\s*\\d+\\s*\\}\\}\\s*\\(\\s*last\\s*version\\s*\\)",
								"$1{{original upload date|$2-$3-$4}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(\\d{4})\\-(\\d{2})\\-(\\d{2})\\s*\\(\\s*first\\s*version\\s*\\)\\;?\\s*(\\d{4})\\-(\\d{2})\\-(\\d{2})\\s*\\(\\s*last\\s*version\\s*\\)",
								"$1{{original upload date|$2-$3-$4}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*\\(?\\s*)(?:Uploaded\\s*on\\s*Commons\\s*at\\s*[\\d\\-]*\\s*[\\d:]*\\s*\\(?UTC\\)?\\s*\\/?\\s*)?Original(?:ly)?\\s*uploaded\\s*at\\s*([\\d\\-]*)\\s*[\\d:]*",
								"$1{{original upload date|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(\\d{1,3}0)\\s*s",
								"$1{{other date|s|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:after|post|بعد|desprès|po|nach|efter|μετά από|después de|pärast|پس از|après|despois do|לאחר|nakon|dopo il|по|na|após|după|после)\\s*(\\d{4})",
								"$1{{other date|after|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:before|vor|pre|до|vör|voor|prior to|ante|antes de|قبل|Преди|abans|před|før|πριν από|enne|پیش از|ennen|avant|antes do|לפני|prije|prima del|пред|przed|înainte de|ранее|pred|före)[\\s\\-]*(\\d{4})",
								"$1{{other date|before|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(\\d{4})\\s*(?:or|أو|o|nebo|eller|oder|ή|ó|või|یا|tai|ou|או|vagy|または|или|അഥവാ|of|lub|ou|sau|или|ali|หรือ|和)\\s*?(\\d{4})",
								"$1{{other date|or|$2|$3}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:sometime\\s*)?(?:between)\\s*(\\d{4})\\s*(?:and|\\-)?\\s*?(\\d{4})",
								"$1{{other date|between|$2|$3}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:primavera(?:\\s*de)?|jaro|forår|frühling|spring|printempo|Kevät|printemps|пролет|Vörjohr|früh[ \\-]?jahr|voorjaar|wiosna|primăvara(?:\\s*lui)?|весна|pomlad|våren|spring)\\s*(\\d{4})",
								"$1{{other date|spring|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:estiu|léto|somero|verano|Kesä|été|verán|estate|лето|zomer|lato|verão(?:\\s*de)?|vara(?:\\s*lui)?|poletje|sommaren|sommer|summer)\\s*(\\d{4})",
								"$1{{other date|summer|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:fall|autumn|tardor|podzim|Efterår|Herbst|aŭtuno|otoño|Syksy|outono(?:\\s*de)?automne|outono|autunno|есен|Harvst|herfst|jesień|toamna(?:\\s*lui)?|осень|jesen|hösten)\\s*(\\d{4})",
								"$1{{other date|fall|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:winter|hivern|zima|Vinter|vintro|invierno|Talvi|hiver|inverno(?:\\s*de)?|зима|iarna(?:\\s*lui)?|зима|zima|vintern)\\s*(\\d{4})",
								"$1{{other date|winter|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:[zc]ir[kc]a|ungefähr|about|around|vers|حوالي|cca|etwa|περ\\.?|cerca\\s*de|حدود|noin|cara a|oko|około|около|c[\\:\\. ]?a?[\\:\\. ]?)\\s*(\\d{3,4})\\s*(?:\\-\\s*(?:[zc]ir[kc]a|ungefähr|about|around|vers|حوالي|cca|etwa|περ\\.?|cerca\\s*de|حدود|noin|cara a|oko|około|около|c[\\:\\. ]?a?[\\:\\. ]?)?\\s*(\\d{3,4}))?",
								"$1{{other date|circa|$2|$3}}")
						.replaceAll(
								caseInsensitive
										+ "(\\{\\{other date\\|circa\\|\\d+)\\|\\}\\}",
								"$1}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)(?:[zc]ir[kc]a|ungefähr|about|around|vers|حوالي|cca|etwa|περ\\.?|cerca\\s*de|حدود|noin|cara a|oko|około|около|c[\\:\\. ]?a?[\\:\\. ]?)\\s*(\\d{3,4})",
								"$1{{other date|circa|$2}}")
						.replaceAll(
								caseInsensitive
										+ "(\\|\\s*date\\s*=\\s*)\\{\\{\\s*ISOdate\\s*\\|\\s*([\\d\\-]+)\\s*\\}\\}\\s*\\(\\s*from\\s*metadata\\s*\\)",
								"$1{{according to EXIF|$2}}");
				if (!(textPart.equals(cleanText))) {
					if (!editSummary
							.contains("[[Com:Regex#Dates|Standardizing dates]]. "))
						this.editSummary = editSummary
								+ "[[Com:Regex#Dates|Standardizing dates]]. ";
					textPart = cleanText;
				}
				cleanText = textPart.replaceAll("__ *NOTOC *__", "")
				// This is commented out due to the fact stated at
				// https://commons.wikimedia.org/wiki/Commons:Regex#Junk_cleanup
				// .replaceAll(caseInsensitive+"\\{\\{en *(?:\\| *1=)? *\\}\\} *(\\||\\}\\}|\\r|\\n)","$1")
				;
				textPart = cleanText;
				// only minor cleanup per
				// https://commons.wikimedia.org/wiki/Commons:Regex#Junk_cleanup
			}
			// only minor cleanup per
			// https://commons.wikimedia.org/wiki/Commons:Regex#Formatting
			textPart = textPart.replaceAll("\\n{3,}", "\n\n");
			cleanText = textPart
					.replaceAll(
							caseInsensitive
									+ "\\[https?://([a-z0-9\\-]{2,3})\\.(?:(w)ikipedia|(wikt)ionary|wiki(n)ews|wiki(b)ooks|wiki(q)uote|wiki(s)ource|wiki(v)ersity|wiki(voy)age)\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
							"[[$2$3$4$5$6$7$8$9:$1:$10|$11]]")
					.replaceAll(
							caseInsensitive
									+ "\\[https?://(?:(m)eta|(incubator)|(quality))\\.wikimedia\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
							"[[$1$2$3:$4|$5]]")
					.replaceAll(
							caseInsensitive
									+ "\\[https?://commons\\.wikimedia\\.(?:com|net|org)/wiki/([^\\]\\[{|}\\s\"]*) +([^\\n\\]]+)\\]",
							"[[:$1|$2]]");
			if (!(textPart.equals(cleanText))) {
				if (!editSummary
						.contains("[[Com:Regex#Links|Standardizing interwikilinks]]. "))
					this.editSummary = editSummary
							+ "[[Com:Regex#Links|Standardizing interwikilinks]]. ";
				textPart = cleanText;
			}
			cleanText = textPart
					.replaceAll(
							caseInsensitive
									+ " *\\[\\[category *: *([^]]*?) *(\\|[^]]*)?\\]\\] *",
							"[[Category:$1$2]]")
					.replaceAll(
							caseInsensitive
									+ "\\[\\[category: *\\]\\](?:\\n( *\\[\\[category:))?",
							"$1");
			cleanText = multipleReplaceAll(
					cleanText,
					caseInsensitive
							+ "\\[\\[category:([^]]+)\\]\\] *\\[\\[category:([^]]+)\\]\\]",
					"[[Category:$1]]\n[[Category:$2]]");
			cleanText = multipleReplaceAll(cleanText, caseInsensitive
					+ "(\\[\\[category:)([^]]+\\]\\])(.*?)\\1\\2\\n?", "$1$2$3");
			if (!(textPart.equals(cleanText))) {
				if (!editSummary
						.contains("[[Com:Regex#Categories|Category-cleanup]]. "))
					this.editSummary = editSummary
							+ "[[Com:Regex#Categories|Category-cleanup]]. ";
				textPart = cleanText;
			}
			textPart = multipleReplaceAll(textPart, caseInsensitive
					+ "(\\[\\[category:[^]]+\\]\\]\\n)\\n+(\\[\\[category:)",
					"$1$2");
			cleanText = textPart
					.replaceAll(caseInsensitive + "</?br( )?(/)?\\\\?>",
							"<br$1$2>")
					.replaceAll(
							caseInsensitive
									+ "(\\{\\{\\}\\}|\\[\\[\\]\\]|<gallery></gallery>|\\[\\[:?File *: *\\]\\])",
							"");
			if (!(textPart.equals(cleanText))) {
				if (!editSummary
						.contains("[[Com:Regex#Formatting|Format-cleanup]]. "))
					this.editSummary = editSummary
							+ "[[Com:Regex#Formatting|Format-cleanup]]. ";
				textPart = cleanText;
			}
			text[i] = textPart;
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
			{
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
		// Category array derived from the pageText!
		Category[] parentCategories = getParentCatsNoDupes();
		boolean cleanupAnyway = !editSummary.isEmpty()
				|| duplicateCategoryCleanup;
		// String array returned via the API, no duplicate entries, no sortkey,
		// no prefix
		String[] allGrandparentCategories;
		{
			String[] pageCategories = wiki.getCategories(name, false,
					ignoreHidden);
			if (pageCategories.length == 1 && !cleanupAnyway)
				// no way of COM:OVERCAT and nothing to clean up
				return;
			Set<String> listSet = all_grand_parentCats(wiki, pageCategories,
					depth, ignoreHidden);
			allGrandparentCategories = listSet.toArray(new String[listSet
					.size()]);
		}

		Object[] cleanedCatsAndText = returnCleanedCatsAndText(cleanupAnyway,
				ignoreHidden, depth, parentCategories, allGrandparentCategories);
		Category[] cleanParentCategories = (Category[]) cleanedCatsAndText[0];
		String removedCategoriesWikitext = (String) cleanedCatsAndText[1];
		String cleanCategoryWikitext = (String) cleanedCatsAndText[2];

		int numberOfRemovedCategories = parentCategories.length
				- cleanParentCategories.length;
		if (cleanupAnyway || numberOfRemovedCategories > 0) {
			// Removes the categories from the text
			for (Category z : parentCategories)
				replaceAllInPagetext(
						"(?iu)" + "\\[\\[Category:" + "\\Q" + z.getName()
								+ "\\E" + "(\\|[^}#\\]\\[{><]*)?" + "\\]\\]",
						"");
			this.setPlainText((getPlainText() + cleanCategoryWikitext)
					.replaceAll("\\n{3,}", "\n\n"));
			this.parents = cleanParentCategories;
			if (numberOfRemovedCategories > 0) {
				String logSummary = "Removed "
						+ numberOfRemovedCategories
						+ " categories which are [[COM:OVERCAT|parent]] of already present categories: "
						+ removedCategoriesWikitext + ". ";
				this.editSummary = logSummary + getEditSummary();
			} else if (duplicateCategoryCleanup)
				// At least clean up duplicate categories, if no OVERCAT found
				this.editSummary = getEditSummary()
						+ "Removed duplicate categories. ";
		}
	}

	/**
	 * Checks if the file has more than one not hidden category, otherwise the
	 * file gets marked with {{subst:unc}}. (In case there is _no_ category at
	 * all, the file gets additionally marked with another maintenance category
	 * and subsequently should be manually examined.)
	 * 
	 * @throws IOException
	 * 
	 */
	public void cleanupUndercat() throws IOException {
		// TODO fetch request _may_ already been done
		// -> save them for later use and use them now!
		String[] allCategories = wiki.getCategories(this.getName(), false,
				false);
		String[] allNotHiddenCategories = wiki.getCategories(this.getName(),
				false, true);
		// count the number of not hidden categories which likely serve only for
		// {{UNC}}-maintenance
		int UNCtotal = 0;
		int UNChidden = 0;
		for (String c : allCategories)
			if (c.contains("needing categories")) {
				++UNCtotal;
				String[] parentsTemp = wiki.getCategories(c, false, true);
				for (String t : parentsTemp)
					if (t.equals("Category:Hidden categories"))
						++UNChidden;
			}
		if (allNotHiddenCategories.length - (UNCtotal - UNChidden) > 1) {
			// Very likely we have _two_ valid not hidden categories
			String plainText = this.getPlainText();
			// Regex stolen from
			// https://commons.wikimedia.org/wiki/MediaWiki:Gadget-HotCat.js
			String uncatRegexp = "\\{\\{\\s*([Uu]ncat(egori[sz]ed( image)?)?|[Nn]ocat|[Nn]eedscategory)[^}]*\\}\\}\\s*(<\\!--.*?--\\>)?";
			String cleanPlainText = plainText.replaceAll(uncatRegexp, "");
			if (plainText.length() != cleanPlainText.length()) {
				this.editSummary = getEditSummary()
						+ (allNotHiddenCategories.length - (UNCtotal - UNChidden))
						+ " visible categories: removed {{uncategorized}}. ";
				this.setPlainText(cleanPlainText);
			}
		}
		if (allNotHiddenCategories.length == 0 && UNCtotal == 0) {
			// likely we do _not_ have the {{unc}} template
			this.setPlainText(this.getPlainText() + "\n{{subst:unc}}");
			this.editSummary = getEditSummary()
					+ "Marked as [[CAT:UNCAT|uncategorized]]. ";
		}
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
	 * @throws IOException
	 */
	private Object[] returnCleanedCatsAndText(boolean cleanupAnyway,
			boolean ignoreHidden, int depth, Category[] parentCategories,
			String[] grandparentStrings) throws IOException {
		Category[] cleanCategories = new Category[parentCategories.length];
		String categoryWikitext = "";
		String removedCatsWikitext = "";

		int revokedCounter = 0;
		String revokedFlag = "e7db5f37c0a2bc9b525d8ab86ea9ed12";
		// calculate the number of redundant categories
		for (int i = 0; i < parentCategories.length; i++) {
			cleanCategories[i] = new Category(parentCategories[i].getName(),
					parentCategories[i].getSortkey()); // clone
			for (int r = 0; r < grandparentStrings.length; r++) {
				if ((parentCategories[i].getName().equals(grandparentStrings[r]
						.split(":", 2)[1]))) {
					removedCatsWikitext = removedCatsWikitext
							+ "[[:Category:"
							+ parentCategories[i].getName()
							+ "]]"
							+ childrenOfRemovedCat("Category:"
									+ parentCategories[i].getName(), depth,
									ignoreHidden) + ", ";
					revokedCounter++;
					cleanCategories[i].setName(revokedFlag);
					break;
				}
			}
		}
		// create a new array for the clean categories if needed
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
						removedCatsWikitext.length() - 2);
			}
			cleanCategories = cleanCategoriesReturn;
		}
		return new Object[] { cleanCategories, removedCatsWikitext,
				categoryWikitext };
	}

	/**
	 * Returns all children of the removed parent (to be used in the "extended"
	 * summary)
	 * 
	 * @param removedParent
	 *            The removed parent
	 * @param ignoreHidden
	 *            If hidden categories are ignored
	 * @return A string containing all children of the removed parent
	 * @throws IOException
	 */
	private String childrenOfRemovedCat(String removedParent, int depth,
			boolean ignoreHidden) throws IOException {
		if (depth > 1)
			return "";
		String returnString = " which is parent of ";
		String[] pageCategories = wiki.getCategories(name, false, ignoreHidden);
		for (String pc : pageCategories) {
			String[] parentsOfPC = wiki.getCategories(pc, false, ignoreHidden);
			for (String potc : parentsOfPC) {
				if (potc.equals(removedParent)) {
					returnString = returnString + "[[:" + pc + "]] and ";
					break;
				}
			}
		}
		return returnString.substring(0, returnString.length() - 5);
	}

	/**
	 * Replace all matches of the regex with the replacement string in the text
	 * string. Ignore all pre, nowiki or comments
	 * 
	 * @param regex
	 *            The regex pattern
	 * @param replacement
	 *            All matches get substituted by this
	 * @return
	 */
	public void replaceAllInPagetext(String regex, String replacement) {
		for (int p = 0; p < text.length; ++p) {
			if (text[p].equals("true"))/* || text[p].equals("<code>")) */{
				++p;
				text[p] = text[p].replaceAll(regex, replacement);
			} else
				++p;
		}
	}

	/**
	 * Returns a List with 2*n elements where n in {1, 2, 3, ... }. The elements
	 * with odd "indexes" indicate whether the String at the next even "index"
	 * should be edited or not. (Has "true" if the next element can be edited
	 * and the prefix if the next element should not be edited) [prefix may be
	 * pre (in <>), nowiki (in <>) or <!--]
	 * 
	 * @param text
	 *            The text to be tokenized
	 * @return A string List with the text in the elements and additional
	 *         indicators before each text element
	 */
	private static LinkedList<String> tokenizeWikitext(String text) {
		LinkedList<String> list = new LinkedList<String>();
		String[][] preserve = {
				{ "<!--", "-->" },
				{ "<nowiki>", "</nowiki>" },
				{ "<pre>", "</pre>" },//
				{ "<source>", "</source>" },
				{ "<syntaxhighlight", "</syntaxhighlight>" },
				{ "<templatedata", "</templatedata>" } };
		// TODO suppress the false positives by rewriting the code somehow.
		// (Note the missing '>')
		// preserve stolen from
		// https://commons.wikimedia.org/w/index.php?diff=113112713

		int smallestIndexOfPrefix = text.length();
		int prefixWithSmallestIndex = -1;
		String textInLowerCase = text.toLowerCase();
		for (int e = 0; e < preserve.length; ++e) {
			int indexOfPrefixE = textInLowerCase.indexOf(preserve[e][0]);
			if (indexOfPrefixE > -1 && indexOfPrefixE < smallestIndexOfPrefix) {
				smallestIndexOfPrefix = indexOfPrefixE;
				prefixWithSmallestIndex = e;
			}
		}
		if (smallestIndexOfPrefix == text.length()) {
			list.add("true");
			list.add(text);
			return list;
		}
		String pre = preserve[prefixWithSmallestIndex][0];
		String suf = preserve[prefixWithSmallestIndex][1];

		String[] split = text.split("(?i)" + pre, 2);
		list.add("true");
		list.add(split[0]);
		String[] split2 = split[1].split("(?i)" + suf, 2);
		list.add(pre);// add pre instead of false!
		if (split2.length == 2) {
			// suffix found
			list.add(pre + split2[0] + suf);
			list.addAll(tokenizeWikitext(split2[1]));
		} else
			// suffix not found
			list.add(pre + split[1]);
		return list;
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
	public static Set<String> all_grand_parentCats(Wiki wiki,
			String[] categories, int depth, boolean ignoreHidden)
			throws IOException {
		if (depth <= 0) {
			Set<String> emptySet = new LinkedHashSet<String>();
			return emptySet;
		}
		Set<String> subSet = new LinkedHashSet<String>();
		for (String cat : categories) {
			String[] tempGrandparent = wiki.getCategories(cat, false,
					ignoreHidden);
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
			String[] parentCats = getParentCatsFromPagetext(true);
			// wipe dupes
			Set<String> names = new HashSet<String>();
			List<Category> catList = new ArrayList<Category>();
			for (String name : parentCats) {
				String splitString[] = name.split("\\|", 2);
				if (names.add(splitString[0])
						&& (!splitString[0].matches("^[ ]*$"))) {
					catList.add(new Category(splitString[0],
							(splitString.length == 2) ? splitString[1] : null));
				} else
					this.duplicateCategoryCleanup = true;
			}
			this.parents = catList.toArray(new Category[catList.size()]);
		}
		return parents;
	}

	/**
	 * Returns the parent categories which can be inferred from the text. (May
	 * include duplicates)
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
	public String[] getParentCatsFromPagetext(boolean sortkey) {
		Matcher m = Pattern
				.compile(
						"\\[\\[[cC]ategory:[^\\|#\\]\\[}{><]+(\\|[^#\\]\\[}{><]*)?\\]\\]")
				.matcher(this.getPlainTextNoComments());
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
	 * Return the pagetext with all comments, pre and nowiki removed
	 * 
	 * @return The altered text which lacks all comments
	 */
	public String getPlainTextNoComments() {
		String returnString = "";
		for (int u = 0; u < text.length; ++u) {
			if (text[u].equals("true"))// || text[u].equals("<code>"))
				returnString += text[++u];
			else
				++u;
		}
		return returnString;
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
		wiki.edit(this.getName(), this.getPlainText(),
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