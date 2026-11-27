"""
Normalisation of the web service paths into endpoints, e.g.
/eva/webservices/contig-alias/v1/chromosomes/md5checksum/7b6e06758e53927330346e9e7cc00cce
 ==> /eva/webservices/contig-alias/v1/chromosomes/md5checksum/{md5}
"""

# Keys are tuples of consecutive path segments; the segment immediately following the match
# is replaced with the placeholder. Longer (more specific) keys take precedence over shorter ones.
CONTEXT_RULES = {
    ('chromosomes', 'md5checksum'): '{md5}',
    ('chromosomes', 'insdc'):       '{insdc_accession}',
    ('chromosomes', 'name'):        '{chromosome_name}',
    ('variants',):                  '{variant_id}',
    ('segments',):                  '{region}',
    ('genes',):                     '{gene_name}',
    ('studies',):                   '{study_accession}',
    ('studies', 'ro-crate'):        '{study_accession}',
    ('species',):                   '{species}',
    ('files',):                     '{file_id}',
    ('clustered-variants',):        '{variant_id}',
    ('submitted-variants',):        '{variant_id}',
    ('chromosome',):                '{chromosome_accession}',
    ('submission',):                '{submission_id}',
    ('assemblies',):                '{assembly_accession}',
    ('genbank',):                   '{genbank_accession}',
    ('chromosomes', 'genbank'):     '{genbank_accession}',
}

_RULE_KEYS = set(CONTEXT_RULES.keys())
# All proper prefixes of rule keys — used to know when to keep accumulating segments.
_RULE_KEY_PREFIXES = {k[:i] for k in _RULE_KEYS for i in range(1, len(k))}


def normalise_path(path):
    """
    Normalise a path by splitting it into segments and applying rules to replace segments with placeholders.
    E.g. /eva/webservices/contig-alias/v1/chromosomes/md5checksum/7b6e06758e53927330346e9e7cc00cce
     ==> /eva/webservices/contig-alias/v1/chromosomes/md5checksum/{md5}
    """
    segments = path.split('/')
    normalised = []
    pending_key = ()   # rule-key prefix accumulated so far
    replace_next = None

    for seg in segments:
        if replace_next is not None:
            normalised.append(replace_next)
            replace_next = None
            continue

        candidate = pending_key + (seg,)
        is_complete = candidate in _RULE_KEYS
        is_prefix   = candidate in _RULE_KEY_PREFIXES

        if is_complete and not is_prefix:
            # Unambiguous complete match — flag next segment for replacement.
            normalised.append(seg)
            replace_next = CONTEXT_RULES[candidate]
            pending_key = ()
        elif is_complete or is_prefix:
            # Complete but extendable, or still just a prefix — keep accumulating.
            normalised.append(seg)
            pending_key = candidate
        else:
            # Dead end: if accumulated pending_key is itself a complete rule,
            # the current segment is the parameter; otherwise pass it through.
            if pending_key in _RULE_KEYS:
                normalised.append(CONTEXT_RULES[pending_key])
            else:
                normalised.append(seg)
            pending_key = ()

    return '/'.join(normalised)
