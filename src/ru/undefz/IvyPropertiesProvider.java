package ru.undefz;

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author undefz
 * @since 1.0
 */
public class IvyPropertiesProvider implements DefaultTemplatePropertiesProvider {

    public static final String DEFAULT_VALUE = "";

    @Override
    public void fillProperties(PsiDirectory psiDirectory, Properties properties) {
        properties.put("IVY_COMPONENT_VERSION", extractIvyVersion(psiDirectory));
    }

    private Object extractIvyVersion(PsiDirectory psiDirectory) {
        final String ivyText = searchIvyConfig(psiDirectory);
        if (ivyText != null)
        {
            final String version = extractDeliverableVersion(ivyText);
            if (version != null && !version.isEmpty())
                return version;
        }
        return DEFAULT_VALUE;
    }

    private String extractDeliverableVersion(String ivyText) {
        try {
            final Document ivyDocument = new SAXBuilder().build(new StringReader(ivyText));
            final Element childElement = ivyDocument.getRootElement().getChild("info");
            if (childElement != null)
                childElement.getAttributeValue("revision");
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
        return DEFAULT_VALUE;
    }

    private String searchIvyConfig(PsiDirectory psiDirectory) {
        if (psiDirectory == null)
            return null;
        final PsiFile ivyFile = psiDirectory.findFile("ivy.xml");
        if (ivyFile != null)
            return ivyFile.getText();
        return searchIvyConfig(psiDirectory.getParentDirectory());
    }
}