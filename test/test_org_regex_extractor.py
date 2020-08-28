import unittest

from extractors import OrgRegexExtractor
from extractors import ExtractionResult

class TestOrgRegexExtractor(unittest.TestCase):

    def test_simple_block(self):
        sut = OrgRegexExtractor("default_output")
        actual = sut.extract_blocks('''
#+BEGIN_SRC
code
#+END_SRC
        ''')
        self.__assert_extraction_result(actual, total_code_blocks=1, total_skipped_blocks=0)
        self.assertEqual(len(actual.blocks), 1)
        self.__assert_block(actual.blocks[0],
            id=0,
            begin_line=1,
            end_line=1,
            skip=False,
            file='default_output',
            data='code',
         )

    def test_two_lines_block(self):
        sut = OrgRegexExtractor("default_output")
        actual = sut.extract_blocks('''
#+BEGIN_SRC
code line 1
code line 2
#+END_SRC
        ''')
        self.__assert_extraction_result(actual, total_code_blocks=1, total_skipped_blocks=0)
        self.assertEqual(len(actual.blocks), 1)
        self.__assert_block(actual.blocks[0],
            id=0,
            begin_line=1,
            end_line=2,
            skip=False,
            file='default_output',
            data='''
code line 1
code line 2
            '''.strip(),
         )

    def test_simple_skip(self):
        sut = OrgRegexExtractor("default_output")
        actual = sut.extract_blocks('''
#+BEGIN_SRC :tangle no
code
#+END_SRC
        ''')
        self.__assert_extraction_result(actual, total_code_blocks=1, total_skipped_blocks=1)
        self.assertEqual(len(actual.blocks), 1)
        self.__assert_block(actual.blocks[0],
            id=0,
            begin_line=1,
            end_line=1,
            skip=True,
            file='default_output',
            data='code',
         )

    def test_skip_with_language(self):
        sut = OrgRegexExtractor("default_output")
        actual = sut.extract_blocks('''
1. create a directory named =.emacs.d= in the base of your home directory;
#+BEGIN_SRC bash :tangle no
mkdir ~/.emacs.d
#+END_SRC
        ''')
        self.__assert_extraction_result(actual, total_code_blocks=1, total_skipped_blocks=1)
        self.assertEqual(len(actual.blocks), 1)
        self.__assert_block(actual.blocks[0],
            id=0,
            begin_line=2,
            end_line=2,
            skip=True,
            file='default_output',
            data='mkdir ~/.emacs.d',
         )

    def __assert_extraction_result(self, actual, **kwargs):
        self.assertTrue(type(actual) == ExtractionResult)
        if 'total_code_blocks' in kwargs:
            self.assertEqual(actual.total_code_blocks, kwargs['total_code_blocks'])
        if 'total_skipped_blocks' in kwargs:
            self.assertEqual(actual.total_skipped_blocks, kwargs['total_skipped_blocks'])

    def __assert_block(self, actual, **kwargs):
        if 'id' in kwargs:
            self.assertEqual(actual.id, kwargs['id'])
        if 'begin_line' in kwargs:
            self.assertEqual(actual.begin_line, kwargs['begin_line'])
        if 'end_line' in kwargs:
            self.assertEqual(actual.end_line, kwargs['end_line'])
        if 'skip' in kwargs:
            self.assertEqual(actual.skip, kwargs['skip'])
        if 'file' in kwargs:
            self.assertEqual(actual.file, kwargs['file'])
        if 'data' in kwargs:
            self.assertEqual(actual.data, kwargs['data'])
