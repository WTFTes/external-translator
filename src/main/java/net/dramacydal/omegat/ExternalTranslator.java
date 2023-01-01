package net.dramacydal.omegat;

import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.Language;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;

public class ExternalTranslator extends BaseTranslate {
    private static String translatorPathProperty = "external_translator_path";
    private static String translatorArgumentsProperty = "external_translator_arguments";

    private static String preferenceName = "allow_external_translator";

    public static void loadPlugins() {
        Core.registerMachineTranslationClass(ExternalTranslator.class);
    }

    @Override
    protected String getPreferenceName() {
        return preferenceName;
    }

    @Override
    public String getName() {
        return "External translator";
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String prev = getFromCache(sLang, tLang, text);
        if (prev != null)
            return prev;

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            String test =
                    // ru-RU ru RU ru_RU Russian (Russia) ru-ru ru_RU
                    tLang.getLanguage() + " " + tLang.getLanguageCode() + " " + tLang.getCountryCode() + " " +
                            tLang.getLocaleCode() + " " + tLang.getDisplayName() + " " + tLang.getLocaleLCID() + " " +
                            tLang.getLocale();

//        String command = "python.exe c:\\scripts\\translate.py {src_lang} {dst_lang} {text} " + "\"" + test + "\"";
            String command = getCredential(translatorPathProperty).trim() + " " + getCredential(translatorArgumentsProperty).trim();
            command = command.replace("{src_lang}", sLang.getLanguageCode());
            command = command.replace("{dst_lang}", tLang.getLanguageCode());
            command = command.replace("{text}", Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)));

            // Windows
            processBuilder.command("cmd.exe", "/c", command);

            Process process = processBuilder.start();

            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            int result = process.waitFor();

            String line;
            LinkedList<String> output = new LinkedList<>();
            while ((line = outputReader.readLine()) != null) {
                output.add(line);
            }

            String errorOutput = "";
            while ((line = errorReader.readLine()) != null) {
                errorOutput += "\n" + line;
            }

            errorOutput = errorOutput.trim();

            if (!errorOutput.isEmpty()) {
                return errorOutput;
            }

            if (output.isEmpty()) {
                return "<empty output>";
            }

            if (result != 0) {
                return String.join(", ", output);
            }

            String translation = new String(Base64.getDecoder().decode(output.get(0)), StandardCharsets.UTF_8);
            this.putToCache(sLang, tLang, text, translation);

            translation = cleanSpacesAroundTags(translation, text);

            return translation;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {
        JPanel externalTranslator1 = new JPanel();
        externalTranslator1.setLayout(new java.awt.GridBagLayout());
        externalTranslator1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 15, 0));
        externalTranslator1.setAlignmentX(0.0F);

        // Info about IAM authentication
        JLabel commandInfoLabel = new JLabel("Path to external translation executable");
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        externalTranslator1.add(commandInfoLabel, gridBagConstraints);

        // API URL
        JLabel pathLabel = new JLabel("Command:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        externalTranslator1.add(pathLabel, gridBagConstraints);

        JTextField pathField = new JTextField(getCredential(translatorPathProperty));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        pathLabel.setLabelFor(pathField);
        externalTranslator1.add(pathField, gridBagConstraints);

        JLabel argumentsInfoLabel = new JLabel("Arguments {src_lang}, {dst_lang}, {text} are templates");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        externalTranslator1.add(argumentsInfoLabel, gridBagConstraints);

        JLabel argumentsInfoLabel2 = new JLabel("to pass source lang, target lang and text to translator");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        externalTranslator1.add(argumentsInfoLabel2, gridBagConstraints);

        // Custom Model
        JLabel argumentsLabel = new JLabel("Arguments:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 5);
        externalTranslator1.add(argumentsLabel, gridBagConstraints);

        JTextField argumentsField = new JTextField(getCredential(translatorArgumentsProperty));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        argumentsLabel.setLabelFor(argumentsField);
        externalTranslator1.add(argumentsField, gridBagConstraints);
        externalTranslator1.setMaximumSize(new Dimension(300, 0));

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean needClearCache = false;
                String path = pathField.getText().trim();
                if (!path.equals(getCredential(translatorPathProperty)))
                    needClearCache = true;
                setCredential(translatorPathProperty, path, false);

                String arguments = argumentsField.getText().trim();
                if (!arguments.equals(getCredential(translatorArgumentsProperty)))
                    needClearCache = true;
                setCredential(translatorArgumentsProperty, arguments, false);

                if (needClearCache)
                    clearCache();
            }
        };

        dialog.panel.valueLabel1.setVisible(false);
        dialog.panel.valueField1.setVisible(false);
        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);
        dialog.panel.temporaryCheckBox.setVisible(false);
        dialog.panel.itemsPanel.add(externalTranslator1);

        dialog.show();
    }
}
