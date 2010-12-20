package tlb.service;

import tlb.TlbConstants;
import tlb.domain.SuiteTimeEntry;
import tlb.service.http.HttpAction;
import tlb.storage.TlbEntryRepository;
import tlb.utils.FileUtil;
import tlb.utils.SystemEnvironment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @understands smoothing test data before posting it to the service
 */
public abstract class SmoothingTalkToService implements TalkToService {
    private static final Logger logger = Logger.getLogger(SmoothingTalkToService.class.getName());
    private final TlbEntryRepository oldTestTimesRepo;

    private static class PassThroughSuiteEntry extends SuiteTimeEntry {
        public PassThroughSuiteEntry() {
            super(null, -1l);
        }

        @Override
        public SuiteTimeEntry smoothedWrt(SuiteTimeEntry newDataPoint, double alpha) {
            return newDataPoint;
        }
    }

    protected final SystemEnvironment environment;
    protected final HttpAction httpAction;

    protected SmoothingTalkToService(SystemEnvironment environment, HttpAction httpAction) {
        this.environment = environment;
        this.httpAction = httpAction;
        FileUtil fileUtil = new FileUtil(this.environment);
        oldTestTimesRepo = new TlbEntryRepository(fileUtil.getUniqueFile("old_test_times"));
    }

    public abstract List<SuiteTimeEntry> fetchLastRunTestTimes();

    public abstract void processedTestClassTime(String className, long time);

    public final void testClassTime(String className, long time) {
        SuiteTimeEntry entry = entryFor(className);
        entry = entry.smoothedWrt(new SuiteTimeEntry(className, time), smoothingFactor());
        processedTestClassTime(entry.getName(), entry.getTime());
    }

    private double smoothingFactor() {
        return Double.parseDouble(environment.val(TlbConstants.Server.SMOOTHING_FACTOR));
    }

    private SuiteTimeEntry entryFor(String className) {
        for (SuiteTimeEntry suiteTimeEntry : getLastRunTestTimes()) {
            if (suiteTimeEntry.getName().equals(className)) {
                return suiteTimeEntry;
            }
        }
        return new PassThroughSuiteEntry();
    }

    public final void clearCachingFiles() {
        try {
            oldTestTimesRepo.cleanup();
        } catch (IOException e) {
            logger.log(Level.WARNING, "failed to delete cachedTestTimes repo", e);
            throw new RuntimeException(e);
        }
        clearOtherCachingFiles();
    }

    protected abstract void clearOtherCachingFiles();

    public final List<SuiteTimeEntry> getLastRunTestTimes() {
        if (!oldTestTimesRepo.getFile().exists()) {
            cacheOldSuiteTimeEntries();
        }
        return SuiteTimeEntry.parse(oldTestTimesRepo.load());
    }

    private void cacheOldSuiteTimeEntries() {
        List<SuiteTimeEntry> suiteTimeEntries = null;
        try {
            suiteTimeEntries = fetchLastRunTestTimes();
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("could not load test times for smoothing.: '%s'", e.getMessage()), e);
            suiteTimeEntries = new ArrayList<SuiteTimeEntry>();
        }
        for (SuiteTimeEntry suiteTimeEntry : suiteTimeEntries) {
            oldTestTimesRepo.appendLine(suiteTimeEntry.dump());
        }
    }
}
