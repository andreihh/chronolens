#!/usr/bin/env python

import sys
import time

if __name__ == "__main__":
    (_, out, err, delay, exit_value) = sys.argv

    sys.stderr.write(err)
    print(out)
    time.sleep(int(delay))
    sys.exit(int(exit_value))

