from extractors import RegexExtractor


class OrgRegexExtractor(RegexExtractor):
    def __init__(self, default_output):
        super().__init__(
            default_output=default_output,
            begin_block_regex=r'^\s*#\+BEGIN_SRC.*$',
            end_block_regex=r'^\s*#\+END_SRC.*$',
            skip_block_regex=r'^\s*#\+BEGIN_SRC.*\s:tangle\ no.*$',
            filename_block_regex=r'^\s*#\+BEGIN_SRC.*\s:tangle\s?(\S*)\s?.*$')
