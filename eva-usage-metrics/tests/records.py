"""Records of the archive datasets, written directly so that the tests of each part do not depend on the others."""
from datetime import datetime


def record(dataset, event_ts, **values):
    """A record of the dataset located in Gaithersburg (US), with the given values and the other columns empty."""
    unknown_columns = set(values) - set(dataset.schema.names)
    assert not unknown_columns, f'{dataset.name} has no columns {unknown_columns}'
    full_record = {name: None for name in dataset.schema.names}
    full_record.update({'country_code': 'US', 'region': 'Maryland', 'city': 'Gaithersburg'})
    full_record.update(values, event_ts=datetime.fromisoformat(event_ts.replace('Z', '+00:00')))
    return full_record
