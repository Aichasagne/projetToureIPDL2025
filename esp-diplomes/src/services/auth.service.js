// Exemple basique pour le service d'authentification
export const authService = {
    login: async (username, password) => {
      // Simuler une API pour le développement
      return new Promise((resolve) => {
        setTimeout(() => {
          if (username === 'admin' && password === 'admin') {
            const userData = {
              id: 'ADMIN001',
              name: 'Administrateur',
              role: 'admin'
            };
            localStorage.setItem('user', JSON.stringify(userData));
            resolve(userData);
          } else if (username === 'student' && password === 'student') {
            const userData = {
              id: 'ESP12345',
              name: 'Étudiant Test',
              role: 'student'
            };
            localStorage.setItem('user', JSON.stringify(userData));
            resolve(userData);
          } else {
            throw new Error('Identifiants invalides');
          }
        }, 1000);
      });
    },
    logout: async () => {
      localStorage.removeItem('user');
      return Promise.resolve();
    },
    getCurrentUser: async () => {
      const user = localStorage.getItem('user');
      return user ? Promise.resolve(JSON.parse(user)) : Promise.resolve(null);
    }
  };