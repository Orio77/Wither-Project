import random
import logging
import scrapy

class RandomUserAgentMiddleware:
    def __init__(self, user_agents):
        self.user_agents = user_agents
        logging.debug(f'Initialized with user agents: {self.user_agents}')

    @classmethod
    def from_crawler(cls, crawler):
        user_agents = crawler.settings.getlist('USER_AGENTS')
        logging.debug(f'Loaded user agents from settings: {user_agents}')
        o = cls(user_agents)
        crawler.signals.connect(o.spider_opened, signal=scrapy.signals.spider_opened)
        return o

    def spider_opened(self, spider):
        self.user_agents = spider.settings.getlist('USER_AGENTS')
        logging.debug(f'User agents after spider opened: {self.user_agents}')

    def process_request(self, request, spider):
        if not self.user_agents:
            logging.error("User agent list is empty, unable to set User-Agent for request.")
            return
        user_agent = random.choice(self.user_agents)
        request.headers['User-Agent'] = user_agent
        logging.debug(f'Using User-Agent: {user_agent}')

class ProxyMiddleware:
    def __init__(self, proxies):
        self.proxies = proxies or []
        logging.debug(f'Initialized with proxies: {self.proxies}')

    @classmethod
    def from_crawler(cls, crawler):
        proxies = crawler.settings.getlist('PROXIES')
        logging.debug(f'Loaded proxies from settings: {proxies}')
        return cls(proxies)

    def spider_opened(self, spider):
        self.proxies = spider.settings.getlist('PROXIES')
        logging.debug(f'Proxies after spider opened: {self.proxies}')

    def process_request(self, request, spider):
        if not self.proxies:
            logging.error("Proxy list is empty, unable to set proxy for request.")
            return
        proxy = random.choice(self.proxies)
        request.meta['proxy'] = proxy
        logging.debug(f'Using proxy: {proxy}')

    def process_exception(self, request, exception, spider):
        logging.error(f'Error with proxy {request.meta.get("proxy", "none")}: {exception}')
        if not self.proxies:
            logging.error("Proxy list is empty, no more proxies to retry.")
            return
        new_proxy = random.choice(self.proxies)
        new_request = request.copy()
        new_request.meta['proxy'] = new_proxy
        logging.debug(f'Retrying with new proxy: {new_proxy}')
        return new_request
    
# mitmproxy --mode transparent -p 8081