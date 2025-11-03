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
  MenuItem,
  Box,
  Alert,
  Paper,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  IconButton,
  Tooltip,
  Stack,
  FormControl,
  InputLabel,
  Select,
  Divider,
} from '@mui/material'
import {
  Visibility,
  FilterList,
  Clear,
  Refresh,
  Logout,
  Timeline,
} from '@mui/icons-material'
import {
  getAllBookings,
  getBookingsByMunicipality,
  getBookingDetails,
  getBookingHistory,
  updateBookingState,
  getBookingsByDistrict,
} from '../services/bookings'
import { fetchDistrict, fetchAllMunicipalities } from '../services/locations'

const StaffDashboard = () => {
  const navigate = useNavigate()
  const [bookings, setBookings] = useState([])
  const [filteredBookings, setFilteredBookings] = useState([])
  const [municipalities, setMunicipalities] = useState([])
  const [districts, setDistricts] = useState([])
  const [selectedDistrict, setSelectedDistrict] = useState('')
  const [selectedMunicipality, setSelectedMunicipality] = useState('')
  const [selectedBooking, setSelectedBooking] = useState(null)
  const [bookingHistory, setBookingHistory] = useState([])
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false)
  const [stateDialogOpen, setStateDialogOpen] = useState(false)
  const [newState, setNewState] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    const token = localStorage.getItem('token')
    const role = localStorage.getItem('role')
    if (!token || role !== 'STAFF') {
      navigate('/login')
      return
    }

    fetchAllBookings()
    loadDistricts()
    loadMunicipalities()
  }, [navigate])

  const loadDistricts = async () => {
    try {
      const cached = localStorage.getItem('districts')
      if (cached) {
        setDistricts(JSON.parse(cached))
      } else {
        const data = await fetchDistrict()
        setDistricts(data)
        localStorage.setItem('districts', JSON.stringify(data))
      }
    } catch (err) {
      console.error('Error loading districts:', err)
      setError('Failed to load districts')
    }
  }

  const loadMunicipalities = async () => {
    try {
      const cached = localStorage.getItem('municipalities')
      if (cached) {
        setMunicipalities(JSON.parse(cached))
      } else {
        const data = await fetchAllMunicipalities()
        setMunicipalities(data)
        localStorage.setItem('municipalities', JSON.stringify(data))
      }
    } catch (err) {
      console.error('Error loading municipalities:', err)
      setError('Failed to load municipalities')
    }
  }

  const fetchAllBookings = async () => {
    setLoading(true)
    try {
      const data = await getAllBookings()
      setBookings(data)
      setFilteredBookings(data)
    } catch (err) {
      console.error('Error fetching bookings:', err)
      setError('Failed to load bookings')
      if (err.response?.status === 401) navigate('/login')
    } finally {
      setLoading(false)
    }
  }

  const applyFilters = async () => {
    try {
      let data = bookings

      if (selectedDistrict) {
        const districtData = await getBookingsByDistrict(selectedDistrict)
        data = districtData
      }

      if (selectedMunicipality) {
        const muniData = await getBookingsByMunicipality(selectedMunicipality)
        data = selectedDistrict
          ? muniData.filter(
              (b) =>
                b.district?.toLowerCase() === selectedDistrict.toLowerCase(),
            )
          : muniData
      }

      setFilteredBookings(data)
    } catch (err) {
      console.error('Error applying filters:', err)
      setError('Failed to apply filters')
    }
  }

  useEffect(() => {
    applyFilters()
  }, [bookings, selectedDistrict, selectedMunicipality])

  const clearFilters = () => {
    setSelectedDistrict('')
    setSelectedMunicipality('')
    setFilteredBookings(bookings)
  }

  const handleViewDetails = async (booking) => {
    try {
      const [details, history] = await Promise.all([
        getBookingDetails(booking.id),
        getBookingHistory(booking.id),
      ])
      setSelectedBooking(details)
      setBookingHistory(history)
      setDetailsDialogOpen(true)
    } catch (err) {
      console.error('Error fetching booking details:', err)
      setError('Failed to load booking details')
    }
  }

  const handleOpenStateDialog = (booking) => {
    setSelectedBooking(booking)
    setNewState('')
    setStateDialogOpen(true)
  }

  const handleUpdateState = async () => {
    if (!newState || !selectedBooking) return

    setLoading(true)
    try {
      await updateBookingState(selectedBooking.id, newState)
      setSuccess('Booking state updated successfully!')
      setStateDialogOpen(false)
      fetchAllBookings()
    } catch (err) {
      console.error('Error updating state:', err)
      setError(
        err.response?.data?.message ||
          err.response?.data ||
          'Failed to update booking state',
      )
    } finally {
      setLoading(false)
    }
  }

  const getAvailableStates = (currentState) => {
    const transitions = {
      RECEIVED: ['ASSIGNED', 'CANCELED'],
      ASSIGNED: ['IN_PROGRESS', 'CANCELED'],
      IN_PROGRESS: ['DONE', 'CANCELED'],
      DONE: [],
      CANCELED: [],
    }
    return transitions[currentState] || []
  }

  const getStateColor = (state) => {
    const colors = {
      RECEIVED: 'info',
      ASSIGNED: 'warning',
      IN_PROGRESS: 'secondary',
      DONE: 'success',
      CANCELED: 'error',
    }
    return colors[state] || 'default'
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('districts')
    localStorage.removeItem('municipalities')
    navigate('/login')
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 4,
        }}
      >
        <Typography variant="h4">Staff Dashboard</Typography>
        <Button
          variant="outlined"
          color="error"
          startIcon={<Logout />}
          onClick={handleLogout}
        >
          Logout
        </Button>
      </Box>

      {/* Alerts */}
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

      {/* Filters */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={2}
          alignItems="center"
        >
          <FilterList />
          <Typography variant="h6">Filters</Typography>

          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>District</InputLabel>
            <Select
              id="staff-filter-district"
              value={selectedDistrict}
              label="District"
              onChange={(e) => setSelectedDistrict(e.target.value)}
            >
              <MenuItem value="">All Districts</MenuItem>
              {districts.map((d) => (
                <MenuItem
                  key={d}
                  value={d}
                  id={`staff-district-option-${d.toLowerCase()}`}
                >
                  {d}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Municipality</InputLabel>
            <Select
              id="staff-filter-municipality"
              value={selectedMunicipality}
              label="Municipality"
              onChange={(e) => setSelectedMunicipality(e.target.value)}
            >
              <MenuItem value="">All Municipalities</MenuItem>
              {selectedDistrict
                ? municipalities
                    .filter((m) => m.district === selectedDistrict)
                    .map((m) => (
                      <MenuItem
                        key={m.name}
                        value={m.name}
                        id={`staff-municipality-option-${m.name.toLowerCase()}`}
                      >
                        {m.name}
                      </MenuItem>
                    ))
                : municipalities.map((m) => (
                    <MenuItem
                      key={m.name}
                      value={m.name}
                      id={`staff-municipality-option-${m.name.toLowerCase()}`}
                    >
                      {m.name}
                    </MenuItem>
                  ))}
            </Select>
          </FormControl>

          <Button
            variant="outlined"
            startIcon={<Clear />}
            onClick={clearFilters}
            disabled={!selectedMunicipality}
          >
            Clear
          </Button>

          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={fetchAllBookings}
          >
            Refresh
          </Button>
        </Stack>
      </Paper>

      {/* Bookings Table */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>
          All Bookings ({filteredBookings.length})
        </Typography>

        {loading ? (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography>Loading...</Typography>
          </Box>
        ) : filteredBookings.length === 0 ? (
          <Typography
            color="text.secondary"
            sx={{ py: 4, textAlign: 'center' }}
          >
            No bookings found
          </Typography>
        ) : (
          <Box sx={{ overflowX: 'auto' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>District</TableCell>
                  <TableCell>Municipality</TableCell>
                  <TableCell>Village</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Time</TableCell>
                  <TableCell>Created By</TableCell>
                  <TableCell>State</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredBookings.map((booking, index) => (
                  <TableRow key={booking.id}>
                    <TableCell>{booking.id}</TableCell>
                    <TableCell>{booking.district || '-'}</TableCell>
                    <TableCell>{booking.municipality}</TableCell>
                    <TableCell>{booking.village}</TableCell>
                    <TableCell>
                      {new Date(booking.date).toLocaleDateString()}
                    </TableCell>
                    <TableCell>{booking.time}</TableCell>
                    <TableCell>{booking.createdBy}</TableCell>
                    <TableCell>
                      <Chip
                        id={`booking-state-chip-${booking.state.toLowerCase()}`}
                        label={booking.state}
                        color={getStateColor(booking.state)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      <Stack direction="row" spacing={1}>
                        <Tooltip title="View Details">
                          <IconButton
                            id={`view-details-button-${index}`}
                            size="small"
                            color="primary"
                            onClick={() => handleViewDetails(booking)}
                          >
                            <Visibility />
                          </IconButton>
                        </Tooltip>
                        {getAvailableStates(booking.state).length > 0 && (
                          <Button
                            id={`update-state-button-${index}`}
                            size="small"
                            variant="outlined"
                            onClick={() => handleOpenStateDialog(booking)}
                          >
                            Update State
                          </Button>
                        )}
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        )}
      </Paper>

      {/* Booking Details Dialog */}
      <Dialog
        id="booking-details-dialog"
        open={detailsDialogOpen}
        onClose={() => setDetailsDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Booking Details #{selectedBooking?.id}</DialogTitle>
        <DialogContent>
          {selectedBooking && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    District
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.district || '-'}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Municipality
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.municipality}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Village
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.village}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Postal Code
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.postalCode}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Date
                  </Typography>
                  <Typography variant="body1">
                    {new Date(selectedBooking.date).toLocaleDateString()}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Time
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.time}
                  </Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">
                    Description
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.description}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Created By
                  </Typography>
                  <Typography variant="body1">
                    {selectedBooking.createdBy}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">
                    Current State
                  </Typography>
                  <Chip
                    label={selectedBooking.state}
                    color={getStateColor(selectedBooking.state)}
                  />
                </Grid>
              </Grid>

              <Divider sx={{ my: 3 }} />

              <Box
                id="state-history-section"
                sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}
              >
                <Timeline />
                <Typography variant="h6">State History</Typography>
              </Box>

              {bookingHistory.length === 0 ? (
                <Typography color="text.secondary">
                  No history available
                </Typography>
              ) : (
                <Stack spacing={2}>
                  {bookingHistory.map((history) => (
                    <Paper key={history.id} variant="outlined" sx={{ p: 2 }}>
                      <Stack
                        direction="row"
                        justifyContent="space-between"
                        alignItems="center"
                      >
                        <Box>
                          <Chip
                            label={history.state}
                            color={getStateColor(history.state)}
                            size="small"
                            sx={{ mb: 1 }}
                          />
                          <Typography variant="body2" color="text.secondary">
                            Changed by: {history.changedBy}
                          </Typography>
                        </Box>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(history.timestamp).toLocaleString()}
                        </Typography>
                      </Stack>
                    </Paper>
                  ))}
                </Stack>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Update State Dialog */}
      <Dialog
        id="update-state-dialog"
        open={stateDialogOpen}
        onClose={() => setStateDialogOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle>Update Booking State</DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Current state: <strong>{selectedBooking?.state}</strong>
          </Typography>
          <FormControl fullWidth>
            <InputLabel>New State</InputLabel>
            <Select
              id="new-state-select"
              value={newState}
              label="New State"
              onChange={(e) => setNewState(e.target.value)}
            >
              {selectedBooking &&
                getAvailableStates(selectedBooking.state).map((state) => (
                  <MenuItem
                    key={state}
                    value={state}
                    id={`state-option-${state.toLowerCase()}`}
                  >
                    {state}
                  </MenuItem>
                ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStateDialogOpen(false)}>Cancel</Button>
          <Button
            id="confirm-update-state-button"
            onClick={handleUpdateState}
            variant="contained"
            disabled={!newState || loading}
          >
            {loading ? 'Updating...' : 'Update'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  )
}

export default StaffDashboard
