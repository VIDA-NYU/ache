package focusedCrawler.target.repository;

import org.archive.io.warc.WARCRecordInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.target.model.Page;

public class WarcTargetRepository implements TargetRepository {

	private static final Logger logger = LoggerFactory.getLogger(WarcTargetRepository.class);

	private boolean compress = false;

	@Override
	public boolean insert(Page target) {
		WARCRecordInfo warcRecord = new WARCRecordInfo();
		warcRecord.setUrl(target.getURL().toString());

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

}
