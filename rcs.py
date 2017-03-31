import sqlite3  # Create database
import requests # Downloading the website
import re       # Regex matching

# Get HTML for a certain url
def get_html(url):
  r = requests.get('http://www.remotecentral.com/cgi-bin/codes' + url, stream=True, headers={'User-agent': 'Mozilla/5.0'})
  if r.status_code == 200:
    r.raw.decode_content = True
    f = r.raw
  return f.read().decode('ISO-8859-15')

# Regex patterns
brand_pattern  = '<a href="/cgi-bin/codes/(.*)/">(<b>)?(.*)(</b>)?</a> <span class="greytext smalltextc"><b>\(<span class="bluetext">(.*)</span>\)</b></span><br>'
model_pattern  = '<tr valign="top"><td style="padding: 2px 10px 2px 0px;" align="right"><img src="/arw-blue.gif" width="9" height="17"></td><td class="text" style="padding: 2px 0px 2px 0px;"><a href="/cgi-bin/codes/%s/(.*)/">(.*)</a></td></tr>'
button_pattern = '<tr><td width="38%" class="filematchleft"><b>(.*)</b><div class="copyclipboard">\(<a href="javascript:void\(0\)" onclick="window.clipboardData.setData\(\'Text\', HexCode([0-9]+).innerText\);" onmouseover="window.status=\'Copy this hex code to the Windows clipboard.\'; return true" onmouseout="window.status=\'\';">Copy to Clipboard</a>\)</div></td><td width="62%" class="filematchright hexcodes"><span id="HexCode(\d+)">(.*)</span></td></tr>'

# Setup the database file
conn = sqlite3.connect("android_remote/app/src/main/assets/remote.db")
cursor = conn.cursor()

cursor.execute('CREATE TABLE brands (brandid integer primary key autoincrement, name text)')
cursor.execute('CREATE TABLE models (modelid integer primary key autoincrement, name text, brandid integer, FOREIGN KEY(brandid) REFERENCES brand(brandid))')
cursor.execute('CREATE TABLE buttons (buttonid integer primary key autoincrement, name text, pattern text, modelid integer, FOREIGN KEY(modelid) REFERENCES model(modelid)) ')

# Get all brands
brand_html = get_html('/')
brand_matches = re.findall(brand_pattern, brand_html)
brand_index = 0
model_index = 0

# If brands were found
if brand_matches:
  # Iterate over each brand
  for b in brand_matches:
    # Visit the page for current brand
    brand_name = b[0]
    brand_html = get_html('/%s/'%brand_name)

    # Add the brand to the database
    brand_name = brand_name
    cursor.execute('INSERT INTO brands (name) VALUES ("%s")'%brand_name.title().replace('_', ' '))
    brand_index += 1

    # Find all models for the specific brand
    model_matches = re.findall(model_pattern%brand_name,brand_html)

    # For each model,
    for m in model_matches:
      # Add the model in the database
      model_name = m[1]
      cursor.execute('INSERT INTO models (name, brandid) VALUES ("%s",%d)'%(model_name, brand_index))
      model_index += 1

      # Some models may spread the buttons over multiple pages
      current_page = 0
      while True:
        current_page += 1

        # Visit its page
        model_url = m[0]
        model_html = get_html('/%s/%s/page-%d'%(brand_name, model_url, current_page))
        
        # Look for buttons
        button_matches = re.findall(button_pattern, model_html)

        # If there are no buttons,
        #   We've reached the last page.
        if not button_matches:
          break
      
        # If there are buttons on this page,
        else:
          # Add the buttons to the database
          for button in button_matches:
            cursor.execute('INSERT INTO buttons (name, pattern, modelid) VALUES ("%s","%s",%d)'%(button[0].strip(), button[3].strip(), model_index))
        
    # Commit brands, models, and buttons to the database.
    conn.commit()
