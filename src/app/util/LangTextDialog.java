package app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.ilcd.commons.LangString;

import app.M;
import epd.util.Strings;

public class LangTextDialog extends FormDialog {

	private final List<LangString> strings;
	private final boolean multiLine;

	private LangTextDialog(List<LangString> strings, boolean multiLine) {
		super(UI.shell());
		this.strings = strings;
		this.multiLine = multiLine;
	}

	static Optional<List<LangString>> open(List<LangString> initial) {
		return open(initial, false);
	}

	static Optional<List<LangString>> openMultiLine(
			List<LangString> initial) {
		return open(initial, true);
	}

	private static Optional<List<LangString>> open(
			List<LangString> initial, boolean multiLine) {
		var strings = initial == null
				? new ArrayList<LangString>()
				: new ArrayList<>(initial);
		var dialog = new LangTextDialog(strings, multiLine);
		return dialog.open() == OK
				? Optional.of(dialog.strings)
				: Optional.empty();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Edit multi-language text");
	}

	@Override
	protected Point getInitialSize() {
		int height = multiLine ? 500 : 350;
		return new Point(800, height);
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var tk = mForm.getToolkit();
		var body = UI.formBody(mForm.getForm(), tk);
		var boxComp = tk.createComposite(body);
		UI.gridData(boxComp, true, false);
		UI.gridLayout(boxComp, 2);

		strings.stream()
				.map(s -> LangBox.of(s, strings))
				.sorted((box1, box2) -> Strings.compare(box1.lang(), box2.lang()))
				.forEach(box -> box.render(boxComp, tk, multiLine));

		var langComp = tk.createComposite(body);
		langComp.setLayoutData(
				new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		tk.createLabel(langComp, "Add language");
		UI.gridLayout(langComp, 3);
		var combo = new Combo(langComp, SWT.READ_ONLY);
		var comboGrid = UI.gridData(combo, false, false);
		comboGrid.widthHint = 200;
		comboGrid.minimumWidth = 200;
		var btn = tk.createButton(langComp, M.Add, SWT.NONE);
		Controls.onSelect(btn, $ -> {
			new LangBox("pl", "", strings).render(boxComp, tk, multiLine);
			mForm.reflow(true);
		});
	}

	private record LangBox(
			String lang, String val, List<LangString> strings) {

		static LangBox of(LangString s, List<LangString> strings) {
			var lang = Strings.nullOrEmpty(s.getLang())
					? "en"
					: s.getLang();
			return new LangBox(lang, s.getValue(), strings);
		}

		void render(Composite comp, FormToolkit tk, boolean multiLine) {
			var text = multiLine
					? UI.formMultiText(comp, tk, label())
					: UI.formText(comp, tk, label());
			if (val != null) {
				text.setText(val);
			}
			text.addModifyListener($ -> {
				var s = text.getText();
				if (Strings.notEmpty(s)) {
					LangString.set(strings, val, lang);
				} else {
					LangString.remove(strings, lang);
				}
			});
		}

		private String label() {
			var lang = lang();
			if ("?".equals(lang))
				return "?";
			try {
				var loc = Locale.forLanguageTag(lang);
				if (loc == null)
					return lang;
				var name = loc.getDisplayLanguage();
				return Strings.notEmpty(name)
						? lang + " - " + name
						: lang;
			} catch (Exception e) {
				return lang;
			}
		}
	}
}
