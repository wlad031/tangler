class ExtractionResult(object):
    def __init__(self, blocks, total_code_blocks, total_skipped_blocks):
        self.blocks = blocks
        self.total_code_blocks = total_code_blocks
        self.total_skipped_blocks = total_skipped_blocks
