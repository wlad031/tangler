import logging
import os

from config import Configuration, configure_logging
from extractors import OrgRegexExtractor


def run_extraction():
    args = Configuration.create_args_parser().parse_args()
    cfg = Configuration.parse_args(args)
    configure_logging(cfg)
    logger = logging.getLogger(__name__)

    input_file = cfg.filename
    logger.info("Extracting code blocks from {}".format(input_file))
    if not os.path.exists(input_file):
        logger.error("\tFile {} doesn't exist, exiting".format(input_file))
        exit(1)

    default_output = input_file + '.output'
    logger.info("Default file for extracted code blocks: {}".format(default_output))
    extractor = OrgRegexExtractor(default_output)

    with open(input_file, 'r') as f:
        logger.info("Starting reading {} ...".format(input_file))
        result = extractor.extract_blocks(f)

        logger.info("Found {} code blocks".format(len(result.blocks)))

        grouped_blocks = {}
        for block in result.blocks:
            if not block.skip:
                if grouped_blocks.get(block.file) is None:
                    grouped_blocks[block.file] = []
                grouped_blocks[block.file].append(block)

        for output_file in grouped_blocks.keys():
            blocks = grouped_blocks[output_file]
            data = ''
            for block in blocks:
                data += block.data + "\n"
            with open(output_file, 'w') as fw:
                logger.info("Writing code blocks to {} ...".format(output_file))
                fw.write(data)

        info = ''
        for output_file in grouped_blocks.keys():
            blocks = grouped_blocks[output_file]
            info += "- {} code blocks wrote to {}\n".format(len(blocks), output_file)
        info += "- {} code blocks skipped".format(result.total_skipped_blocks)
        logger.info(info)
