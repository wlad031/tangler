import logging


def configure_logging(cfg):
    level = logging.INFO
    if cfg.is_quiet:
        level = logging.ERROR
    if cfg.is_debug:
        level = logging.DEBUG
    logging.basicConfig(level=level, format='%(message)s')
