# Data-Collection-Tool

The monitor used to track data change pattern of apps in App Store. Obtained data will be used as output for the Ranking Detection System.

The app collection tool includes two main functions, **suspicious app monitoring** and **metadata collecting**. 

Suspicious app monitoring aims to detect apps with unusual ranking changes. The tool crawler such ranking data from some third party websites. Besides an app’s basic ranking change information, the tool also retrieves other information such as app’s id and its current chart ranking.

Metadata collecting aims to retrieve the real-time meta-data information such as an app’s rating and review amount through _iTunes Search API_. 
