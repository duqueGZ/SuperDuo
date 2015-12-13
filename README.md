# SuperDuo
Udacity Android Developer Nanodegree. Project 3

In order to make Football Scores app works correctly is needed to configure and use a football-data.org API_KEY.
To get one, you must to sign up for an account (http://api.football-data.org/register)
Since it is not allowed to publicly share your personal API KEY, the code in this repository does not contain mine. So, 
once you have the API KEY, it would be needed to replace 'PLACE-API-KEY-HERE' placeholder in strings.xml resource file
('api_key' string value) by your real and valid API KEY value:

app/src/main/res/values/strings.xml:
```xml
<resources>
    <!-- General strings -->
    <string name="app_name" translatable="false">Football Scores</string>
    <string name="api_key" translatable="false">PLACE-API-KEY-HERE</string> <!--put here your API KEY -->
    ...
</resources>
```
