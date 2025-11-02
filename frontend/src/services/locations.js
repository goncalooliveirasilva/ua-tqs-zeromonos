import axios from 'axios'

export const fetchDistrict = async () => {
  try {
    const res = await axios.get(
      'https://e-redes.opendatasoft.com/api/explore/v2.1/catalog/datasets/districts-portugal/records?limit=20',
    )
    const districts = res.data.results.map((r) => r.dis_name)
    return districts.sort()
  } catch (error) {
    console.log('Error fetching districts:', error)
  }
}

export const fetchAllMunicipalities = async () => {
  let municipalities = []
  let start = 0
  const limit = 100
  let totalRecords = Infinity

  try {
    while (start < totalRecords) {
      const res = await axios.get(
        `https://e-redes.opendatasoft.com/api/explore/v2.1/catalog/datasets/municipalities-portugal/records`,
        { params: { limit, start } },
      )

      municipalities.push(
        ...res.data.results.map((r) => ({
          name: r.con_name,
          district: r.dis_name,
        })),
      )

      totalRecords = res.data.nhits
      start += limit
    }

    return municipalities.sort((a, b) => a.name.localeCompare(b.name))
  } catch (err) {
    console.error('Error fetching municipalities:', err)
    throw new Error('Could not load municipalities')
  }
}

export const fetchVillages = async (municipality) => {
  if (!municipality) return []
  try {
    const res = await axios.get(
      `https://json.geoapi.pt/municipio/${municipality}/freguesias`,
    )
    return res.data.freguesias || []
  } catch (err) {
    console.error('Error fetching villages:', err)
    return []
  }
}
