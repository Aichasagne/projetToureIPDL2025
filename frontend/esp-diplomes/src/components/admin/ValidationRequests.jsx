import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminService } from '../../services/admin.service';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  IconButton,
  Button,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  Tooltip
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import FilterListIcon from '@mui/icons-material/FilterList';
import VisibilityIcon from '@mui/icons-material/Visibility';

const getStatusColor = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'REJECTED':
      return 'error';
    default:
      return 'default';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'COMPLETED':
      return 'Validé';
    case 'PENDING':
      return 'En cours';
    case 'REJECTED':
      return 'Rejeté';
    default:
      return 'Inconnu';
  }
};

const ValidationRequests = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [requests, setRequests] = useState([]);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRequests = async () => {
      try {
        const data = await adminService.getValidationRequests();
        setRequests(data);
      } catch (err) {
        setError('Impossible de charger les demandes de validation. Veuillez réessayer plus tard.');
        console.error('Erreur lors de la récupération des demandes de validation:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchRequests();
  }, []);

  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleSearchChange = (event) => {
    setSearchTerm(event.target.value);
    setPage(0);
  };

  const handleStatusFilterChange = (event) => {
    setStatusFilter(event.target.value);
    setPage(0);
  };

  // Filtrer les demandes
  const filteredRequests = requests.filter((request) => {
    const matchesSearch =
      request.studentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      request.matricule.toLowerCase().includes(searchTerm.toLowerCase()) ||
      request.department.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus =
      statusFilter === 'ALL' || request.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '70vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Demandes de Validation
      </Typography>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <TextField
          label="Rechercher"
          variant="outlined"
          value={searchTerm}
          onChange={handleSearchChange}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
          sx={{ width: '300px' }}
        />
        <FormControl sx={{ width: '200px' }}>
          <InputLabel id="status-filter-label">Filtrer par statut</InputLabel>
          <Select
            labelId="status-filter-label"
            value={statusFilter}
            label="Filtrer par statut"
            onChange={handleStatusFilterChange}
          >
            <MenuItem value="ALL">Tous</MenuItem>
            <MenuItem value="PENDING">En cours</MenuItem>
            <MenuItem value="COMPLETED">Validé</MenuItem>
            <MenuItem value="REJECTED">Rejeté</MenuItem>
          </Select>
        </FormControl>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Étudiant</TableCell>
              <TableCell>Matricule</TableCell>
              <TableCell>Département</TableCell>
              <TableCell>Programme</TableCell>
              <TableCell>Date de soumission</TableCell>
              <TableCell>Statut</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredRequests
              .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
              .map((request) => (
                <TableRow key={request.id}>
                  <TableCell>{request.studentName}</TableCell>
                  <TableCell>{request.matricule}</TableCell>
                  <TableCell>{request.department}</TableCell>
                  <TableCell>{request.program}</TableCell>
                  <TableCell>
                    {new Date(request.submissionDate).toLocaleDateString()}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusText(request.status)}
                      color={getStatusColor(request.status)}
                    />
                  </TableCell>
                  <TableCell>
                    <Tooltip title="Voir les détails">
                      <IconButton onClick={() => navigate(`/admin/requests/${request.id}`)}>
                        <VisibilityIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </TableContainer>

      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={filteredRequests.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Box>
  );
};

export default ValidationRequests;