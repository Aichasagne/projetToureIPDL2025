export const studentService = {
    getDashboardData: async () => {
      // Simuler une API pour le développement
      return new Promise((resolve) => {
        setTimeout(() => {
          const user = JSON.parse(localStorage.getItem('user'));
          resolve({
            student: {
              name: user?.name || 'Étudiant',
              matricule: user?.id || 'ESP12345',
              department: 'Informatique',
              program: 'Licence en Informatique',
              graduationYear: '2023',
            },
            validationStatus: {
              status: 'PENDING',
              progress: 60,
              completedSteps: 3,
              totalSteps: 5,
              latestUpdate: '2023-11-15T10:30:00',
            },
            pendingSignatures: [
              { id: 1, title: 'Directeur des études', status: 'PENDING' },
              { id: 2, title: 'Service comptabilité', status: 'PENDING' },
            ],
            completedSignatures: [
              { id: 3, title: 'Chef de département', status: 'COMPLETED', date: '2023-11-12T14:20:00' },
              { id: 4, title: 'Service scolarité', status: 'COMPLETED', date: '2023-11-10T09:15:00' },
              { id: 5, title: 'Bibliothèque', status: 'COMPLETED', date: '2023-11-05T11:45:00' },
            ],
            notifications: [
              { 
                id: 1, 
                message: 'Votre demande a été validée par le Chef de département',
                date: '2023-11-12T14:20:00',
                read: false
              },
              { 
                id: 2, 
                message: 'Votre demande a été validée par le Service scolarité',
                date: '2023-11-10T09:15:00',
                read: true
              },
            ],
          });
        }, 1000);
      });
    },
    getValidationStatus: async () => {
      // Implémenter selon vos besoins
      return Promise.resolve({
        // Données de statut de validation
      });
    }
  };