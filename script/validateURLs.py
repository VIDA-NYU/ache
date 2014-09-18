# Given a file with a list of urls, one per line,
# this script removes malformed ones and
# uniqs them, outputting the result

# Aline Bessa --- 09/04/2014

import sys
import re

if __name__ == "__main__":

    if len(sys.argv) < 2:
        print "You have to pass a file with a list of urls as an argument."
        exit()

    f = open(sys.argv[1], "r")
    lines = f.readlines()

    for l in lines:
        url = l.strip()
        regex = re.compile(
            r'^(?:http|ftp)s?://' # http:// or https://
            r'(?:(?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\.)+(?:[A-Z]{2,6}\.?|[A-Z0-9-]{2,}\.?)|' #domain...
            r'localhost|' #localhost...
            r'\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})' # ...or ip
            r'(?::\d+)?' # optional port
            r'(?:/?|[/?]\S+)$', re.IGNORECASE)
        if not regex.match(url):
            lines.remove(l)

    lines = list(set(lines))
    for l in lines:
        print l.strip()

    f.close()
    
