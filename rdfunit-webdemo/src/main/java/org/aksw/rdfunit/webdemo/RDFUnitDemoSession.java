package org.aksw.rdfunit.webdemo;

import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import org.aksw.rdfunit.RDFUnitConfiguration;
import org.aksw.rdfunit.tests.TestCase;
import org.aksw.rdfunit.tests.TestSuite;
import org.aksw.rdfunit.tests.executors.TestExecutor;
import org.aksw.rdfunit.tests.generators.TestGeneratorExecutor;
import org.aksw.rdfunit.webdemo.utils.CommonAccessUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Keeps user session variables
 * TODO refactor
 *
 * @author Dimitris Kontokostas
 * @since 11 /15/13 9:52 AM
 */
public class RDFUnitDemoSession extends VaadinSession {


    /**
     * Instantiates a new RDF unit demo session.
     *
     * @param service the service
     */
    public RDFUnitDemoSession(VaadinService service) {
        super(service);
    }

    /**
     * Init session variables.
     */
    public static void init(String clientHost) {

        VaadinSession.getCurrent().setAttribute("client", clientHost);

        String baseDir = _getBaseDir();
        VaadinSession.getCurrent().setAttribute(String.class, baseDir);

        TestGeneratorExecutor testGeneratorExecutor = new TestGeneratorExecutor();
        VaadinSession.getCurrent().setAttribute(TestGeneratorExecutor.class, testGeneratorExecutor);


        TestSuite testSuite = new TestSuite(new ArrayList<TestCase>());
        VaadinSession.getCurrent().setAttribute(TestSuite.class, testSuite);

        CommonAccessUtils.initializeSchemaServices();
    }

    private static String _getBaseDir() {
        File f = VaadinSession.getCurrent().getService().getBaseDirectory();
        return f.getAbsolutePath() + "/data/";
    }

    /**
     * Gets client hostname
     *
     * @return the base dir
     */
    public static String getHostName() {
        return VaadinSession.getCurrent().getAttribute("client").toString();
    }

    /**
     * Gets base directory.
     *
     * @return the base dir
     */
    public static String getBaseDir() {
        return VaadinSession.getCurrent().getAttribute(String.class);
    }

    /**
     * Gets current testGeneratorExecutor.
     *
     * @return the test generator executor
     */
    public static TestGeneratorExecutor getTestGeneratorExecutor() {
        return VaadinSession.getCurrent().getAttribute(TestGeneratorExecutor.class);
    }

    /**
     * Sets current testGeneratorExecutor.
     */
    public static void setTestGeneratorExecutor(TestGeneratorExecutor testGeneratorExecutor) {
        VaadinSession.getCurrent().setAttribute(TestGeneratorExecutor.class, testGeneratorExecutor);
    }

    /**
     * Gets current testExecutor.
     *
     * @return current testExecutor
     */
    public static TestExecutor getTestExecutor() {
        return VaadinSession.getCurrent().getAttribute(TestExecutor.class);
    }

    /**
     * Sets test executor.
     *
     * @param testExecutor the test executor
     */
    public static void setTestExecutor(TestExecutor testExecutor) {
        VaadinSession.getCurrent().setAttribute(TestExecutor.class, testExecutor);
    }

    /**
     * Gets current testSuite.
     *
     * @return current testSuite
     */
    public static TestSuite getTestSuite() {
        return VaadinSession.getCurrent().getAttribute(TestSuite.class);
    }

    /**
     * Sets current testSuite.
     *
     * @param testSuite current testSuite
     */
    public static void setTestSuite(TestSuite testSuite) {
        VaadinSession.getCurrent().setAttribute(TestSuite.class, testSuite);
    }

    /**
     * Sets current RDFUnitConfiguration.
     *
     * @param configuration the configuration
     */
    public static void setRDFUnitConfiguration(RDFUnitConfiguration configuration) {
        VaadinSession.getCurrent().setAttribute(RDFUnitConfiguration.class, configuration);
    }

    /**
     * Gets current RDFUnitConfiguration.
     *
     * @return current RDFUnitConfiguration
     */
    public static RDFUnitConfiguration getRDFUnitConfiguration() {
        return VaadinSession.getCurrent().getAttribute(RDFUnitConfiguration.class);
    }
}
