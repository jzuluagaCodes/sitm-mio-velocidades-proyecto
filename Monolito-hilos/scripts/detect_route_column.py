import csv
from pathlib import Path
from collections import Counter, defaultdict

lines_path = Path("data/sample/lines-241-ActiveGT.csv")
datagrams_path = Path("data/sample/datagrams-MiniPilot.csv")

lineid_to_shortname = {}

with open(lines_path, newline="", encoding="utf-8-sig") as f:
    reader = csv.DictReader(f)
    for row in reader:
        line_id = row["LINEID"].strip()
        shortname = row["SHORTNAME"].strip()
        lineid_to_shortname[line_id] = shortname

active_line_ids = set(lineid_to_shortname.keys())

matches_by_column = Counter()
unique_matches_by_column = defaultdict(set)
top_values_by_column = defaultdict(Counter)

total_rows = 0

with open(datagrams_path, newline="", encoding="utf-8-sig") as f:
    reader = csv.reader(f)

    for row in reader:
        total_rows += 1

        for index, value in enumerate(row):
            value = value.strip().replace('"', "")

            if value in active_line_ids:
                matches_by_column[index] += 1
                unique_matches_by_column[index].add(value)
                top_values_by_column[index][value] += 1

print("Total datagramas:", total_rows)
print("Rutas activas:", len(active_line_ids))
print()

for column, matches in matches_by_column.most_common():
    coverage = matches / total_rows * 100
    unique_count = len(unique_matches_by_column[column])

    print(f"Columna {column}")
    print(f"  Coincidencias: {matches}")
    print(f"  Cobertura: {coverage:.2f}%")
    print(f"  LINEID únicos encontrados: {unique_count}")

    print("  Top valores:")
    for line_id, count in top_values_by_column[column].most_common(10):
        shortname = lineid_to_shortname.get(line_id, "?")
        print(f"    {line_id} ({shortname}): {count}")

    print()