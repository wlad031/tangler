class Block(object):
    def __init__(self, id, begin_line, end_line, skip, file, data):
        self.id = id
        self.begin_line = begin_line
        self.end_line = end_line
        self.skip = skip
        self.file = file
        self.data = data

    def __str__(self):
        return "Block #{} [{}:{}] skip={} file={}".format(
            self.id, self.begin_line, self.end_line, self.skip, self.file)
