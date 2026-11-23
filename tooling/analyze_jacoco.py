import xml.etree.ElementTree as ET

report = r"C:\Users\chadi\Documents\GitHub\-LSII_Sporting\backend\target\site\jacoco\jacoco.xml"
root = ET.parse(report).getroot()
classes = []
for cls in root.findall(".//class"):
    counter = cls.find("counter[@type='BRANCH']")
    if counter is None:
        continue
    missed = int(counter.attrib["missed"])
    covered = int(counter.attrib["covered"])
    total = missed + covered
    if total:
        classes.append((missed, covered, missed / total, cls.attrib["name"].replace("/", ".")))
for missed, covered, missed_ratio, name in sorted(classes, key=lambda item: (-item[0], item[2]))[:25]:
    print(f"missed={missed:2} covered={covered:3} ratio={missed_ratio:.1%} {name}")

