import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Container,
  Typography,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Button,
  TextField,
  MenuItem,
  Box,
  Alert,
  Paper,
  Chip,
  CircularProgress,
} from '@mui/material'
import {
  getMyBookings,
  createBooking,
  cancelBooking,
  getAvailableTimes,
} from '../services/bookings'
import {
  fetchDistrict,
  fetchAllMunicipalities,
  fetchVillages,
} from '../services/locations'

const CitizenDashboard = () => {
  const navigate = useNavigate()
  const [bookings, setBookings] = useState([])
  const [districts, setDistricts] = useState([])
  const [municipalities, setMunicipalities] = useState([])
  const [filteredMunicipalities, setFilteredMunicipalities] = useState([])
  const [villages, setVillages] = useState([])
  const [district, setDistrict] = useState('')
  const [municipality, setMunicipality] = useState('')
  const [village, setVillage] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [date, setDate] = useState('')
  const [time, setTime] = useState('')
  const [description, setDescription] = useState('')
  const [availableTimes, setAvailableTimes] = useState([])
  const [lastBookingToken, setLastBookingToken] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) navigate('/login')

    fetchBookings()
    loadDistricts()
    loadMunicipalities()
  }, [])

  const fetchBookings = async () => {
    try {
      const data = await getMyBookings()
      setBookings(data)
    } catch (err) {
      console.error('Error fetching bookings:', err)
      if (err.response?.status === 403) navigate('/login')
    }
  }

  const loadDistricts = async () => {
    try {
      const cached = localStorage.getItem('districts')
      if (cached) {
        setDistricts(JSON.parse(cached))
      } else {
        const districtsData = await fetchDistrict()
        setDistricts(districtsData)
        localStorage.setItem('districts', JSON.stringify(districtsData))
      }
    } catch (err) {
      console.error('Error loading districts:', err)
    }
  }

  const loadMunicipalities = async () => {
    try {
      const cached = localStorage.getItem('municipalities')
      if (cached) {
        const allMuni = JSON.parse(cached)
        setMunicipalities(allMuni)
        setFilteredMunicipalities(allMuni)
      } else {
        const allMuni = await fetchAllMunicipalities()
        setMunicipalities(allMuni)
        setFilteredMunicipalities(allMuni)
        localStorage.setItem('municipalities', JSON.stringify(allMuni))
      }
    } catch (err) {
      console.error('Error loading municipalities:', err)
    }
  }

  useEffect(() => {
    if (district) {
      setFilteredMunicipalities(
        municipalities.filter((m) => m.district === district),
      )
    } else {
      setFilteredMunicipalities(municipalities)
    }
  }, [district, municipalities])

  const handleMunicipalityChange = async (muni) => {
    setMunicipality(muni)
    setVillage('')
    setAvailableTimes([])
    const villagesData = await fetchVillages(muni)
    setVillages(villagesData)
  }

  const fetchAvailableTimes = async () => {
    if (!municipality || !date) return
    try {
      const times = await getAvailableTimes(municipality, date)
      setAvailableTimes(times)
      if (times.length === 0)
        setError(
          'No available times for this date. Please select another date.',
        )
    } catch (err) {
      console.error('Error fetching available times:', err)
      setError('Could not fetch available times')
    }
  }

  const handleCreateBooking = async (e) => {
    e.preventDefault()
    if (
      !district ||
      !municipality ||
      !village ||
      !postalCode ||
      !date ||
      !time ||
      !description
    ) {
      setError('Please fill in all fields')
      return
    }

    setLoading(true)
    setError('')
    setSuccess('')

    try {
      const response = await createBooking({
        district,
        municipality,
        village,
        postalCode,
        date,
        time,
        description,
      })
      setLastBookingToken(response.token)
      setSuccess('Booking created successfully!')

      setDistrict('')
      setMunicipality('')
      setVillage('')
      setPostalCode('')
      setDate('')
      setTime('')
      setDescription('')
      setAvailableTimes([])
      setVillages([])
      fetchBookings()
    } catch (err) {
      console.error('Error creating booking:', err)
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          'Error creating booking.',
      )
    } finally {
      setLoading(false)
    }
  }

  const handleCancelBooking = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) return
    try {
      await cancelBooking(id)
      setSuccess('Booking cancelled successfully!')
      fetchBookings()
    } catch (err) {
      console.error('Error canceling booking:', err)
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          'Error cancelling booking',
      )
    }
  }

  const copyToken = () => {
    navigator.clipboard.writeText(lastBookingToken)
    setSuccess('Token copied to clipboard!')
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('districts')
    localStorage.removeItem('municipalities')
    navigate('/login')
  }

  const getStateColor = (state) => {
    switch (state) {
      case 'RECEIVED':
        return 'info'
      case 'ASSIGNED':
        return 'success'
      case 'IN_PROG':
        return 'warning'
      case 'DONE':
        return 'success'
      case 'CANCELED':
        return 'error'
      default:
        return 'default'
    }
  }

  return (
    <Container sx={{ mt: 4, mb: 4 }}>
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 4,
        }}
      >
        <Typography variant="h4">Citizen Dashboard</Typography>
        <Button variant="outlined" color="error" onClick={handleLogout}>
          Logout
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>
          {success}
        </Alert>
      )}

      {/* My Bookings */}
      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h5" gutterBottom>
          My Bookings
        </Typography>
        {bookings.length === 0 ? (
          <Typography color="text.secondary" sx={{ mt: 2 }}>
            No bookings yet. Create your first booking below!
          </Typography>
        ) : (
          <Box sx={{ overflowX: 'auto' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>District</TableCell>
                  <TableCell>Municipality</TableCell>
                  <TableCell>Village</TableCell>
                  <TableCell>Postal Code</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Time</TableCell>
                  <TableCell>Description</TableCell>
                  <TableCell>State</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {bookings.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell>{b.district || '-'}</TableCell>
                    <TableCell>{b.municipality}</TableCell>
                    <TableCell>{b.village}</TableCell>
                    <TableCell>{b.postalCode}</TableCell>
                    <TableCell>
                      {new Date(b.date).toLocaleDateString()}
                    </TableCell>
                    <TableCell>{b.time}</TableCell>
                    <TableCell>
                      <Typography
                        variant="body2"
                        sx={{
                          maxWidth: 200,
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        {b.description}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={b.state}
                        color={getStateColor(b.state)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {b.state === 'RECEIVED' ? (
                        <Button
                          color="error"
                          variant="contained"
                          size="small"
                          onClick={() => handleCancelBooking(b.id)}
                        >
                          Cancel
                        </Button>
                      ) : (
                        <Typography variant="caption" color="text.secondary">
                          Cannot cancel
                        </Typography>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        )}
      </Paper>

      {/* Booking Token */}
      {lastBookingToken && (
        <Paper sx={{ p: 3, mb: 4, bgcolor: 'success.light' }}>
          <Alert severity="success" sx={{ mb: 2 }}>
            <Typography variant="h6" gutterBottom>
              Booking Created Successfully!
            </Typography>
            <Typography variant="body2" gutterBottom>
              Your booking token is:{' '}
              <strong id="booking-token">{lastBookingToken}</strong>
            </Typography>
            <Typography variant="caption">
              Save this token to check your booking status without logging in.
            </Typography>
          </Alert>
          <Button variant="contained" onClick={copyToken}>
            Copy Token
          </Button>
        </Paper>
      )}

      {/* Create Booking Form */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>
          Create a New Booking
        </Typography>
        <Box
          component="form"
          onSubmit={handleCreateBooking}
          sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}
        >
          {/* District */}
          <TextField
            select
            label="District"
            id="booking-district"
            value={district}
            onChange={(e) => {
              setDistrict(e.target.value)
              setMunicipality('')
              setVillage('')
              setAvailableTimes([])
              setVillages([])
            }}
            required
            helperText="Select your district"
          >
            {districts.map((d) => (
              <MenuItem
                key={d}
                value={d}
                id={`district-option-${d.toLowerCase()}`}
              >
                {d}
              </MenuItem>
            ))}
          </TextField>

          {/* Municipality */}
          <TextField
            select
            label="Municipality"
            id="booking-municipality"
            value={municipality}
            onChange={(e) => handleMunicipalityChange(e.target.value)}
            required
            disabled={!district || filteredMunicipalities.length === 0}
            helperText={
              !district
                ? 'Select a district first'
                : filteredMunicipalities.length === 0
                  ? 'Loading municipalities...'
                  : 'Select your municipality'
            }
          >
            {filteredMunicipalities.map((m) => (
              <MenuItem
                key={m.name}
                value={m.name}
                id={`municipality-option-${m.name.toLowerCase()}`}
              >
                {m.name}
              </MenuItem>
            ))}
          </TextField>

          {/* Village */}
          <TextField
            select
            label="Village (Freguesia)"
            id="booking-village"
            value={village}
            onChange={(e) => setVillage(e.target.value)}
            required
            disabled={!municipality || villages.length === 0}
            helperText={
              !municipality
                ? 'Select a municipality first'
                : villages.length === 0
                  ? 'Loading villages...'
                  : 'Select your village'
            }
          >
            {villages.map((v) => (
              <MenuItem
                key={v}
                value={v}
                id={`village-option-${v.toLowerCase()}`}
              >
                {v}
              </MenuItem>
            ))}
          </TextField>

          {/* Postal Code */}
          <TextField
            id="booking-postalcode"
            label="Postal Code"
            placeholder="0000-000"
            value={postalCode}
            onChange={(e) => setPostalCode(e.target.value)}
            required
          />

          {/* Date */}
          <TextField
            type="date"
            label="Date"
            id="booking-date"
            InputLabelProps={{ shrink: true }}
            value={date}
            onChange={(e) => {
              setDate(e.target.value)
              setTime('')
              setAvailableTimes([])
            }}
            onBlur={fetchAvailableTimes}
            inputProps={{ min: new Date().toISOString().split('T')[0] }}
            required
            helperText="Select a date to see available times"
          />

          {/* Time */}
          <TextField
            select
            id="booking-time"
            label="Time"
            value={time}
            onChange={(e) => setTime(e.target.value)}
            required
            disabled={availableTimes.length === 0}
            helperText={
              !date
                ? 'Select a date first'
                : availableTimes.length === 0
                  ? 'No available times for this date'
                  : 'Select your preferred time'
            }
          >
            {availableTimes.map((t) => (
              <MenuItem
                key={t}
                value={t}
                id={`time-option-${t.replace(':', '')}`}
              >
                {t}
              </MenuItem>
            ))}
          </TextField>

          {/* Description */}
          <TextField
            label="Description"
            id="booking-description"
            multiline
            rows={3}
            placeholder="Describe the items you want to collect..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
            helperText="Provide details about what needs to be collected"
          />

          {/* Submit Button */}
          <Button
            id="booking-submit"
            type="submit"
            variant="contained"
            size="large"
            disabled={loading || availableTimes.length === 0}
            startIcon={loading && <CircularProgress size={20} />}
          >
            {loading ? 'Creating...' : 'Create Booking'}
          </Button>
        </Box>
      </Paper>
    </Container>
  )
}

export default CitizenDashboard
