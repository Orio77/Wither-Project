from flask import Flask, jsonify, request
from scraper import Scraper
from cleaner import Cleaner
from ai_processing_service import AIProcessingService
import logging

app = Flask(__name__)

# Configure logging
logging.basicConfig(level=logging.DEBUG)

@app.route('/scrape', methods=['POST'])
def scrape():
    """
    Endpoint to scrape, clean, and process data from provided URLs.

    Returns:
        JSON response containing processed questions and answers.
    """
    scraper = Scraper()
    cleaner = Cleaner()
    ai_processing = AIProcessingService()

    links = request.json  # List of Strings ["", "", "", ...]
    logging.debug("PYPS: Received links: %s", links)

    try:
        logging.debug("PYPS: Performing action... scraping data")
        data = scraper.scrape(links)  # Dict of 'url: html_data' {url: html_data, ...}

        cleaned_data = {}  # Dict of 'url: pure_data' {url: pure_data, ...}

        logging.debug("PYPS: Performing action... Cleaning data")
        for url, html_data in data.items():
            logging.debug("PYPS: Cleaning: %s of source: %s", html_data, url)
            pure_data = cleaner.clean(html_data)
            cleaned_data[url] = pure_data

        logging.debug("PYPS: Done. Data Cleaned")

        qas = []

        logging.debug("PYPS: Performing action... Extracting Questions and Answers out of the data")

        for url, pure_data in cleaned_data.items():
            logging.debug("PYPS: Processing data of: %s %s", url, pure_data)
            processed_data = ai_processing.process_data(pure_data)

            qa = {
                "url": url,
                "Question": processed_data['Question'],
                "Text": processed_data['Text']
            }

            logging.debug("PYPS: Created qa: %s", qa)

            qas.append(qa)

        logging.debug("PYPS: Done. Data extracted.")
        return jsonify(qas)

    except Exception as e:
        logging.error("PYPS: An error occurred: %s", e)
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)