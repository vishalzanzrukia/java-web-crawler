# java-web-crawler
This is open source web crawler example based on Java technologies with following features.

- Auto Restart after once cycle finished
- Configuration to set time between two cycles
- Capability to start crawling process with same state in case of JVM crash/down or Server crash/down where it left while crash/shutdown occurred.
- Configurations to run crawler process with different zones of single site. The current crawler is running with 7 zones successfully on production env.
- Configuration to set zone wise different set of url filters
- Configuration to set zone wise different parsers
- Configuration to set robots.txt rules enable/disable
- Configuration to set maximum url visit per second
- Configuration to set maximum depth to visit
- Configuration to set maximum bytes per page to download 
- Sitemaps parsing support
- Retry support with parsing

It's still ongoing project, not ready to use yet.
