import json
import time

global CI
global input
global config

def parseDate(date):
    if (isinstance(date, int)):
        return time.strftime('%Y-%m-%dT%H:%M:%SZ', time.localtime(date))
    else:
        return date


def parse_simple0(data):
    if data:
        return CI("test.simple0", {
            'three': data['three'] if 'three' in data else None,
            'two': data['two'] if 'two' in data else None,
            'six': parseDate(data['six'] if 'six' in data else None),
            'five': parse_simple1(data['five'] if 'five' in data else None),
            '_name': map(lambda x: parse_simple1(x), data['name'] if 'name' in data else None),
            'one': data['one'] if 'one' in data else None
        })
    else:
        return None

def parse_simple1(data):
    if data:
        return CI("test.simple1", {
            'x': data['x'] if 'x' in data else None,
            'y': data['y'] if 'y' in data else None
        })
    else:
        return None


def parse_simple(data):
    if data:
        return CI("test.simple", {
            '_id': data['id'] if 'id' in data else None,
            '_type': data['type'] if 'type' in data else None,
            'c': data['c'] if 'c' in data else None,
            'd': parse_simple0(data['d'] if 'd' in data else None)
        })
    else:
        return None

output = parse_simple(json.loads(input.content))