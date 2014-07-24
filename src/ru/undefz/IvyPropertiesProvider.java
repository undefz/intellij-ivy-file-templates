package ru.undefz;

import com.google.common.base.Optional;
import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.apache.log4j.Logger;
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
    private static final Logger log = Logger.getLogger(IvyPropertiesProvider.class);


    public static final String DEFAULT_VALUE = "";

    @Override
    public void fillProperties(PsiDirectory psiDirectory, Properties properties) {
        log.info("Starting ivy scanning process");
        final String propertyValue = extractIvyVersion(psiDirectory);
        log.info("Founded value " + propertyValue);
        properties.put("IVY_COMPONENT_VERSION", propertyValue);
    }

    private String extractIvyVersion(PsiDirectory psiDirectory) {
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
        log.info("Parsing ivy file");
        try {
            final Document ivyDocument = new SAXBuilder().build(new StringReader(ivyText));
            final Element childElement = ivyDocument.getRootElement().getChild("info");
            if (childElement != null)
                return childElement.getAttributeValue("revision");
        } catch (JDOMException | IOException e) {
            throw new RuntimeException(e);
        }
        return DEFAULT_VALUE;
    }

    private String searchIvyConfig(PsiDirectory psiDirectory) {
        if (psiDirectory == null)
            return null;
        log.info("Scanning directory " + psiDirectory.getName());
        final PsiFile ivyFile = psiDirectory.findFile("ivy.xml");
        if (ivyFile != null)
        {
            log.info("Found ivy file " + ivyFile.getName());
            return ivyFile.getText();
        }

        if ("test".equals(psiDirectory.getName()))
        {
            final PsiDirectory parent = psiDirectory.getParentDirectory();
            if (parent != null)
            {
                final PsiDirectory sourceDirectory = parent.findSubdirectory("source");
                if (sourceDirectory != null)
                {
                    return searchIvyConfig(sourceDirectory);
                }
            }
        }
        return searchIvyConfig(psiDirectory.getParentDirectory());
    }
}