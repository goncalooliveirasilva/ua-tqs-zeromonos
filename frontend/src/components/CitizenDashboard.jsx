import { useState, useEffect } from 'react'
import axios from 'axios'
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
  Stack,
} from '@mui/material'
import {
  getMyBookings,
  createBooking,
  cancelBooking,
  getAvailableTimes,
} from '../services/bookings'

const CitizenDashboard = () => {
  const [bookings, setBookings] = useState([])
  const [municipalities, setMunicipalities] = useState([])
  const [villages, setVillages] = useState([])
  const [municipality, setMunicipality] = useState('')
  const [village, setVillage] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [date, setDate] = useState('')
  const [time, setTime] = useState('')
  const [description, setDescription] = useState('')
  const [availableTimes, setAvailableTimes] = useState([])
  const [lastBookingToken, setLastBookingToken] = useState('')

  useEffect(() => {
    fetchBookings()
    fetchMunicipalities()
  }, [])

  const fetchBookings = async () => {
    try {
      const data = await getMyBookings()
      setBookings(data)
    } catch (err) {
      console.error('Error fetching bookings:', err)
    }
  }

  const fetchMunicipalities = async () => {
    try {
      const res = await axios.get('https://json.geoapi.pt/municipios')
      console.log(res)
      setMunicipalities(res.data)
    } catch (err) {
      console.error('Error fetching municipalities:', err)
    }
  }

  const fetchVillages = async (muni) => {
    if (!muni) return
    try {
      const res = await axios.get(
        `https://json.geoapi.pt/municipio/${muni}/freguesias`,
      )
      console.log(res)
      setVillages(res.data.freguesias)
    } catch (err) {
      console.error('Error fetching villages:', err)
    }
  }

  const fetchAvailableTimes = async () => {
    if (!municipality || !date) return
    const times = await getAvailableTimes(municipality, date)
    setAvailableTimes(times)
  }

  const handleCreateBooking = async (e) => {
    e.preventDefault()
    if (
      !municipality ||
      !village ||
      !postalCode ||
      !date ||
      !time ||
      !description
    )
      return

    try {
      const response = await createBooking({
        municipality,
        village,
        postalCode,
        date,
        time,
        description,
      })

      setLastBookingToken(response.token)

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
    }
  }

  const handleCancelBooking = async (id) => {
    try {
      await cancelBooking(id)
      fetchBookings()
    } catch (err) {
      console.error('Error canceling booking:', err)
    }
  }

  const copyToken = () => {
    navigator.clipboard.writeText(lastBookingToken)
  }

  return (
    <Container sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        My Bookings
      </Typography>

      <Table sx={{ mb: 4 }}>
        <TableHead>
          <TableRow>
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
              <TableCell>{b.municipality}</TableCell>
              <TableCell>{b.village}</TableCell>
              <TableCell>{b.postalCode}</TableCell>
              <TableCell>{b.date}</TableCell>
              <TableCell>{b.time}</TableCell>
              <TableCell>{b.description}</TableCell>
              <TableCell>{b.state}</TableCell>
              <TableCell>
                {b.state === 'RECEIVED' && (
                  <Button
                    color="error"
                    variant="contained"
                    size="small"
                    onClick={() => handleCancelBooking(b.id)}
                  >
                    Cancel
                  </Button>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {lastBookingToken && (
        <Stack sx={{ mb: 4 }}>
          <Alert severity="success">
            Booking created! Your token is: <strong>{lastBookingToken}</strong>
          </Alert>
          <Button
            variant="outlined"
            onClick={copyToken}
            sx={{ mt: 1, width: '200px' }}
          >
            Copy Token
          </Button>
        </Stack>
      )}

      <Typography variant="h5" gutterBottom>
        Create a Booking
      </Typography>

      <Box
        component="form"
        onSubmit={handleCreateBooking}
        sx={{ display: 'flex', flexDirection: 'column', gap: 2, mb: 4 }}
      >
        <TextField
          select
          label="Municipality"
          value={municipality}
          onChange={(e) => {
            setMunicipality(e.target.value)
            setVillage('')
            fetchVillages(e.target.value)
          }}
          required
        >
          {municipalities.map((m) => (
            <MenuItem key={m} value={m}>
              {m}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          select
          label="Village"
          value={village}
          onChange={(e) => setVillage(e.target.value)}
          required
          disabled={!municipality}
        >
          {villages.map((v) => (
            <MenuItem key={v} value={v}>
              {v}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Postal Code"
          value={postalCode}
          onChange={(e) => setPostalCode(e.target.value)}
          required
        />

        <TextField
          type="date"
          label="Date"
          InputLabelProps={{ shrink: true }}
          value={date}
          onChange={(e) => setDate(e.target.value)}
          onBlur={fetchAvailableTimes}
          required
        />

        <TextField
          select
          label="Time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
          required
        >
          {availableTimes.map((t) => (
            <MenuItem key={t} value={t}>
              {t}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          required
        />

        <Button type="submit" variant="contained">
          Create Booking
        </Button>
      </Box>
    </Container>
  )
}

export default CitizenDashboard
