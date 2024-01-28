# Explain PostgreSQL plugin for Eclipse and Dbeaver

Analyzes EXPLAIN plan from PostgreSQL and related (Greenplum, Citus, TimescaleDB and Amazon RedShift).
<br><br>
Shows plan and node details and visualizations with piechart, flowchart and tilemap, also gives smart recommendations to improve query.
<p>Uses the public api from the <a href="https://explain.tensor.ru">explain.tensor.ru</a> , the site can be changed in Preferences.
<p>Uses the <a href="https://marketplace.eclipse.org/content/chromium-integration-eclipse">Chromium Integration for Eclipse</a> to open the site.</p>

<p><a href="https://explain.tensor.ru/about">Learn more</a>
<br><br>
<h3>Usage:</h3>
<ul>
    <li>Database Development:</li>
    <ul>
        <li>Format query - select "Format SQL" from the context menu of the SQL editor
        </li>
        <img src="screenshot-1.png" width="50%" height="50%">
        <li>Explain plan - select "Get Execution Plan" from the context menu of the SQL editor
        </li>
        <img src="screenshot-2.png" width="50%" height="50%">
    </ul>
    <li>DBeaver</li>
    <ul>
        <li>Format query - select "Format SQL" from the context menu of the SQL editor
        </li>
        <img src="screenshot-3.png" width="50%" height="50%">
        <li>Explain plan - select "Execute | Explain Execution Plan" from the context menu.
        </li>
        <img src="screenshot-4.png" width="50%" height="50%">
    </ul>
    <li>Preferences</li>
    <ul>
        <li>
        Select "Explain PostgreSQL" from Eclipse's Preferences.
        </li>
        <img src="screenshot-5.png" width="50%" height="50%">
    </ul>
</ul>
<br>
<h3>Install</h3>
From the Eclipse menu, choose Help -> Install new software and enter the URL:
<br><code>https://explain.tensor.ru/downloads/plugins/eclipse/</code>
<br>
<p><a href="https://n.sbis.ru/explain">Support</a>
