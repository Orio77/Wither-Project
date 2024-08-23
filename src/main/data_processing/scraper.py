import os
import sys
import json
import scrapy
from scrapy.crawler import CrawlerProcess
from scrapy.signalmanager import dispatcher
from scrapy import signals
from scrapy.utils.project import get_project_settings
import logging

# Debugging: Print current working directory and classpath
print("Current working directory:", os.getcwd())
print("Classpath:", sys.path)

# Initialize the logger
logging.basicConfig(
    filename='scraper.log',  # Log to a file
    filemode='a',  # Append to the file
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    level=logging.DEBUG  # Set the log level to DEBUG
)
logger = logging.getLogger(__name__)

class Scraper:

    def scrape(self, urls):
        logger.info("SCPR: Starting the scraping process for URLs: %s", urls)
        scraped_data = {}

        def crawler_results(signal, sender, item, response, spider):
            logger.info("SCPR: Scraped data from URL: %s", response.url)
            scraped_data[response.url] = item['body_text']

        dispatcher.connect(crawler_results, signal=signals.item_scraped)

        process = CrawlerProcess(get_project_settings())
        process.crawl(TextSpider, urls=urls)
        process.start()  # the script will block here until the crawling is finished
        logger.info("SCPR: Scraping process completed")

        # Save the scraped data to a file
        if (scraped_data):
            file_path = os.path.join(os.path.dirname(__file__), 'data.json')
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(scraped_data, f, ensure_ascii=False, indent=4)
            logger.info("SCPR: Scraped data saved to %s", file_path)
        else:
            logger.info("SCPR: Failed to scrape data from web")

        return scraped_data


class TextSpider(scrapy.Spider):
    name = "textspider"

    def __init__(self, urls, *args, **kwargs):
        super(TextSpider, self).__init__(*args, **kwargs)
        self.start_urls = urls

    def parse(self, response):
        logger.debug("SCPR: Parsing response from URL: %s", response.url)
        body_text = response.xpath('//body//text()').getall()
        body_text = [text.strip() for text in body_text if text.strip()]
        yield {
            'body_text': ' '.join(body_text)
        }


# Example usage:
if __name__ == "__main__":
    scraper = Scraper()
    urls = ['https://www.blinkist.com/en/books/how-to-think-more-effectively-en', 'https://fs.blog/how-to-think/']
    data = scraper.scrape(urls)
    logger.info("SCPR: Scraped data: %s", data)
    print(data)