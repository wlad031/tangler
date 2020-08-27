import argparse


class Configuration(object):
    def __init__(self, filename, is_quiet, is_debug):
        self.filename = filename
        self.is_quiet = is_quiet
        self.is_debug = is_debug

    @staticmethod
    def create_args_parser():
        parser = argparse.ArgumentParser(description='File tangler')
        parser.add_argument('-f', '--file', required=True, type=str,
                            help='a file to be tangled')
        parser.add_argument('-q', '--quiet', default=False, action='store_true',
                            help="run in quiet mode")
        parser.add_argument('--debug', default=False, action='store_true',
                            help="run in debug mode")
        return parser

    @staticmethod
    def parse_args(args):
        return Configuration(args.file, args.quiet, args.debug)
